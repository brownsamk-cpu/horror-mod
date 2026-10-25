package com.horrormod.castle;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.ChainBlock;

/**
 * Castle LEVEL SIX. The layout is DATA (data/horrormod/castle/level_six.json, made by tools/castle_level_six.py):
 * one picture of letters per height layer. This class just reads the letters and places the blocks.
 * Local directions: x = right, y = up, z = forward. Everything is written "facing north" and then turned to face your way.
 */
public final class CastleLevelSix {
	public static final String DATA = "/data/horrormod/castle/level_six.json";

	/** What got built, so tests (and the item) can find things. */
	public record Built(BlockPos origin, Direction forward, List<Stray> strays) {
	}

	private static JsonObject data;

	private CastleLevelSix() {
	}

	public static synchronized JsonObject data() {
		if (data == null) {
			try (InputStream in = CastleLevelSix.class.getResourceAsStream(DATA)) {
				data = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			} catch (Exception e) {
				throw new IllegalStateException("Castle Level 6 data missing: " + DATA, e);
			}
		}
		return data;
	}

	public static int width() {
		return data().getAsJsonArray("size").get(0).getAsInt();
	}

	public static int padJump() {
		return data().get("pad_jump").getAsInt();
	}

	/** Turns a letter from the data into a block (written as if you face north). */
	private static BlockState block(char ch) {
		return switch (ch) {
			case '#' -> Blocks.STONE_BRICKS.defaultBlockState();
			case 'd' -> Blocks.DEEPSLATE_BRICKS.defaultBlockState();
			case 't' -> Blocks.DEEPSLATE_TILES.defaultBlockState();
			case 'c' -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
			case 'm' -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
			case 'b' -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
			case 'G' -> Blocks.GOLD_BLOCK.defaultBlockState();
			case 'w' -> Blocks.WATER.defaultBlockState();
			case 'L' -> Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);
			case 'l' -> Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);
			case '^' -> Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
			case 'u' -> Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
			case 'v' -> Blocks.VINE.defaultBlockState().setValue(VineBlock.NORTH, true);
			case 'H' -> Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH);
			case '=' -> Blocks.IRON_CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.Z);
			case 'C', 'K' -> Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
			case 'o' -> Blocks.DARK_OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, 0);
			case 'p' -> CastleBlocks.STRAY_BLAST_PAD.defaultBlockState().setValue(StrayBlastPadBlock.FACING, Direction.NORTH);
			case 'g' -> CastleBlocks.LEVEL_SEVEN_GATE.defaultBlockState();
			default -> Blocks.AIR.defaultBlockState();
		};
	}

	public static Rotation rotationFor(Direction forward) {
		return switch (forward) {
			case EAST -> Rotation.CLOCKWISE_90;
			case SOUTH -> Rotation.CLOCKWISE_180;
			case WEST -> Rotation.COUNTERCLOCKWISE_90;
			default -> Rotation.NONE;
		};
	}

	/** World position of a local (x right, y up, z forward) spot. */
	public static BlockPos toWorld(BlockPos origin, Direction forward, int x, int y, int z) {
		Direction right = forward.getClockWise();
		return origin.offset(right.getStepX() * x + forward.getStepX() * z, y, right.getStepZ() * x + forward.getStepZ() * z);
	}

	/** Where the course starts if the player stands at {@code feet} looking {@code forward}: the front door is right in front of them. */
	public static BlockPos originInFrontOf(BlockPos feet, Direction forward) {
		Direction left = forward.getCounterClockWise();
		return feet.relative(forward, 2).relative(left, width() / 2).below();
	}

	public static Built build(ServerLevel level, BlockPos origin, Direction forward) {
		JsonObject d = data();
		Rotation rot = rotationFor(forward);
		JsonArray layers = d.getAsJsonArray("layers");
		List<BlockPos> strayAt = new ArrayList<>();
		List<BlockPos> blockEntities = new ArrayList<>();
		// pass 1: solid stuff; pass 2: things that hang on other blocks (vines, ladders, lanterns, signs)
		for (int pass = 0; pass < 2; pass++) {
			for (int y = 0; y < layers.size(); y++) {
				JsonArray rows = layers.get(y).getAsJsonArray();
				for (int z = 0; z < rows.size(); z++) {
					String row = rows.get(z).getAsString();
					for (int x = 0; x < row.length(); x++) {
						char ch = row.charAt(x);
						boolean attached = "vHLlo".indexOf(ch) >= 0;
						if (attached != (pass == 1)) {
							continue;
						}
						BlockPos pos = toWorld(origin, forward, x, y, z);
						level.setBlock(pos, block(ch).rotate(rot), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
						if (ch == 'S') {
							strayAt.add(pos);
						} else if (ch == 'C' || ch == 'K' || ch == 'o') {
							blockEntities.add(pos);
						}
					}
				}
			}
		}
		// chests and signs
		JsonObject signs = d.getAsJsonObject("signs");
		for (int y = 0; y < layers.size(); y++) {
			JsonArray rows = layers.get(y).getAsJsonArray();
			for (int z = 0; z < rows.size(); z++) {
				String row = rows.get(z).getAsString();
				for (int x = 0; x < row.length(); x++) {
					char ch = row.charAt(x);
					BlockPos pos = toWorld(origin, forward, x, y, z);
					if ((ch == 'C' || ch == 'K') && level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
						chest.setItem(13, new ItemStack(ch == 'C' ? CastleItems.ZIP_LINE_HOOK : CastleItems.LEVEL_SEVEN_KEY));
					} else if (ch == 'o' && level.getBlockEntity(pos) instanceof SignBlockEntity sign && signs.has(x + "," + y + "," + z)) {
						JsonArray lines = signs.getAsJsonArray(x + "," + y + "," + z);
						List<Component> msg = new ArrayList<>();
						for (int i = 0; i < 4; i++) {
							msg.add(Component.literal(i < lines.size() ? lines.get(i).getAsString() : ""));
						}
						sign.setText(new SignText(msg, msg, DyeColor.WHITE, true), SignTextSlot.FRONT);
						sign.setWaxed(true);
						sign.setChanged();
						level.sendBlockUpdated(pos, sign.getBlockState(), sign.getBlockState(), Block.UPDATE_CLIENTS);
					}
				}
			}
		}
		// the strays: they stay on their ledges (can't walk), never despawn, and wear pumpkins so the sun doesn't bother them
		List<Stray> strays = new ArrayList<>();
		BlockPos middle = toWorld(origin, forward, width() / 2, 14, 18);
		for (BlockPos pos : strayAt) {
			Stray stray = net.minecraft.world.entity.EntityTypes.STRAY.create(level, EntitySpawnReason.MOB_SUMMONED);
			if (stray == null) {
				continue;
			}
			double dx = middle.getX() - pos.getX(), dz = middle.getZ() - pos.getZ();
			float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
			stray.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0.0F);
			stray.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.MOB_SUMMONED, null);
			stray.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
			stray.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
			stray.setDropChance(EquipmentSlot.HEAD, 0.0F);
			stray.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
			stray.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
			stray.setYHeadRot(yaw);
			stray.setPersistenceRequired();
			stray.reassessWeaponGoal();
			level.addFreshEntity(stray);
			strays.add(stray);
		}
		return new Built(origin, forward, strays);
	}
}

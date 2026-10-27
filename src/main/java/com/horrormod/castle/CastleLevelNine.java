package com.horrormod.castle;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariants;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

/**
 * Castle LEVEL NINE (June's design): the find-the-hidden-buttons puzzle room. The layout is DATA
 * (data/horrormod/castle/level_nine.json, made by tools/castle_level_nine.py). Local x = right, y = up, z = forward,
 * written "facing north" and turned to face your way. Not stacked on the tower yet (Level 8 isn't designed). No cats!
 */
public final class CastleLevelNine {
	public static final String DATA = "/data/horrormod/castle/level_nine.json";

	public record Built(BlockPos origin, Direction forward, BlockPos start, List<BlockPos> buttons, Painting painting, List<Skeleton> skeletons) {
	}

	private static JsonObject data;

	private CastleLevelNine() {
	}

	public static synchronized JsonObject data() {
		if (data == null) {
			try (InputStream in = CastleLevelNine.class.getResourceAsStream(DATA)) {
				data = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			} catch (Exception e) {
				throw new IllegalStateException("Castle Level 9 data missing: " + DATA, e);
			}
		}
		return data;
	}

	public static int width() {
		return data().getAsJsonArray("size").get(0).getAsInt();
	}

	public static int height() {
		return data().getAsJsonArray("size").get(1).getAsInt();
	}

	public static int length() {
		return data().getAsJsonArray("size").get(2).getAsInt();
	}

	private static BlockState button(AttachFace face, Direction facing) {
		return CastleBlocks.HIDDEN_BUTTON.defaultBlockState().setValue(ButtonBlock.FACE, face).setValue(ButtonBlock.FACING, facing);
	}

	private static BlockState block(char ch) {
		return switch (ch) {
			case '#' -> Blocks.STONE_BRICKS.defaultBlockState();
			case 'd' -> Blocks.DEEPSLATE_BRICKS.defaultBlockState();
			case 'm' -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
			case 'p' -> Blocks.OAK_PLANKS.defaultBlockState();
			case 'k' -> Blocks.BOOKSHELF.defaultBlockState();
			case 'r' -> Blocks.CARPET.red().defaultBlockState();
			case 'h' -> Blocks.HAY_BLOCK.defaultBlockState();
			case 'w' -> Blocks.SPRUCE_PLANKS.defaultBlockState();
			case 'f' -> Blocks.POTTED_POPPY.defaultBlockState();
			case 'G' -> Blocks.GOLD_BLOCK.defaultBlockState();
			case 'L' -> Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);
			case 'H' -> Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST);
			case 'C' -> Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
			case 'o' -> Blocks.DARK_OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, 8);
			case 's' -> CastleBlocks.SECRET_DOOR.defaultBlockState();
			case '1', '2', '3', '4', '5' -> CastleBlocks.PUZZLE_LAMP.defaultBlockState().setValue(PuzzleLampBlock.NUMBER, ch - '0');
			case 'E' -> button(AttachFace.WALL, Direction.EAST);
			case 'K' -> button(AttachFace.WALL, Direction.WEST);
			case 'N' -> button(AttachFace.WALL, Direction.SOUTH);
			case 'F' -> button(AttachFace.FLOOR, Direction.SOUTH);
			default -> Blocks.AIR.defaultBlockState();
		};
	}

	private static BlockPos local(JsonArray a) {
		return new BlockPos(a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt());
	}

	public static BlockPos startLocal() {
		return local(data().getAsJsonArray("start"));
	}

	/** Local spots of the 5 hidden buttons, in June's order (painting, carpet, bookshelf, ladder, flower pot). */
	public static List<BlockPos> buttonsLocal() {
		List<BlockPos> out = new ArrayList<>();
		data().getAsJsonArray("buttons").forEach(e -> out.add(local(e.getAsJsonArray())));
		return out;
	}

	/** The player stands at {@code feet} looking {@code forward}: the front door is just ahead, floor level with the ground. */
	public static BlockPos originInFrontOf(BlockPos feet, Direction forward) {
		Direction left = forward.getCounterClockWise();
		return feet.relative(forward, 1).relative(left, width() / 2).below();
	}

	public static BlockPos toWorld(BlockPos origin, Direction forward, BlockPos local) {
		return CastleLevelSix.toWorld(origin, forward, local.getX(), local.getY(), local.getZ());
	}

	public static Built build(ServerLevel level, BlockPos origin, Direction forward) {
		JsonObject d = data();
		Rotation rot = CastleLevelSix.rotationFor(forward);
		JsonArray layers = d.getAsJsonArray("layers");
		for (int pass = 0; pass < 2; pass++) {
			for (int y = 0; y < layers.size(); y++) {
				JsonArray rows = layers.get(y).getAsJsonArray();
				for (int z = 0; z < rows.size(); z++) {
					String row = rows.get(z).getAsString();
					for (int x = 0; x < row.length(); x++) {
						char ch = row.charAt(x);
						boolean attached = "LoEKNFHfr".indexOf(ch) >= 0;
						if (attached != (pass == 1)) {
							continue;
						}
						level.setBlock(CastleLevelSix.toWorld(origin, forward, x, y, z), block(ch).rotate(rot), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
					}
				}
			}
		}
		JsonObject signs = d.getAsJsonObject("signs");
		for (String k : signs.keySet()) {
			String[] p = k.split(",");
			BlockPos pos = CastleLevelSix.toWorld(origin, forward, Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
			if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
				JsonArray lines = signs.getAsJsonArray(k);
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
		for (int y = 0; y < layers.size(); y++) {
			JsonArray rows = layers.get(y).getAsJsonArray();
			for (int z = 0; z < rows.size(); z++) {
				String row = rows.get(z).getAsString();
				for (int x = 0; x < row.length(); x++) {
					if (row.charAt(x) == 'C' && level.getBlockEntity(CastleLevelSix.toWorld(origin, forward, x, y, z)) instanceof ChestBlockEntity chest) {
						chest.setItem(13, new ItemStack(CastleItems.LEVEL_TEN_KEY));
					}
				}
			}
		}
		// the little painting that hangs right over button 1
		BlockPos paintPos = toWorld(origin, forward, local(d.getAsJsonArray("painting")));
		Painting painting = new Painting(level, paintPos, rot.rotate(Direction.EAST),
				level.registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT).getOrThrow(PaintingVariants.KEBAB));
		level.addFreshEntity(painting);
		// June's extra: skeletons guard the puzzle room (they stay; never in the secret room)
		List<Skeleton> skeletons = new ArrayList<>();
		for (var e : d.getAsJsonArray("skeletons")) {
			BlockPos p = toWorld(origin, forward, local(e.getAsJsonArray()));
			Skeleton s = net.minecraft.world.entity.EntityTypes.SKELETON.create(level, EntitySpawnReason.STRUCTURE);
			if (s != null) {
				s.snapTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, rot.rotate(Direction.SOUTH).toYRot(), 0);
				s.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
				s.setPersistenceRequired();
				level.addFreshEntity(s);
				skeletons.add(s);
			}
		}
		List<BlockPos> buttons = new ArrayList<>();
		buttonsLocal().forEach(b -> buttons.add(toWorld(origin, forward, b)));
		return new Built(origin, forward, toWorld(origin, forward, startLocal()), buttons, painting, skeletons);
	}
}

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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Castle LEVEL SEVEN (June's design). The layout is DATA (data/horrormod/castle/level_seven.json, made by
 * tools/castle_level_seven.py): one picture of letters per height layer. This class reads the letters and places the blocks.
 * Local directions: x = right, y = up, z = forward, written "facing north" and turned to face your way. No cats!
 */
public final class CastleLevelSeven {
	public static final String DATA = "/data/horrormod/castle/level_seven.json";

	public record Built(BlockPos origin, Direction forward, BlockPos start) {
	}

	private static JsonObject data;

	private CastleLevelSeven() {
	}

	public static synchronized JsonObject data() {
		if (data == null) {
			try (InputStream in = CastleLevelSeven.class.getResourceAsStream(DATA)) {
				data = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			} catch (Exception e) {
				throw new IllegalStateException("Castle Level 7 data missing: " + DATA, e);
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

	public static int floor() {
		return data().get("floor").getAsInt();
	}

	private static BlockState block(char ch) {
		return switch (ch) {
			case '#' -> Blocks.STONE_BRICKS.defaultBlockState();
			case 'd' -> Blocks.DEEPSLATE_BRICKS.defaultBlockState();
			case 't' -> Blocks.DEEPSLATE_TILES.defaultBlockState();
			case 'c' -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
			case 'm' -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
			case 'b' -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
			case 'G' -> Blocks.GOLD_BLOCK.defaultBlockState();
			case 'L' -> Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);
			case 'l' -> Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false);
			case '~' -> Blocks.LAVA.defaultBlockState();
			case 'r' -> CastleBlocks.RESCUE_FIELD.defaultBlockState();
			case 'e' -> CastleBlocks.ELECTRA_CORE.defaultBlockState().setValue(ElectraCoreBlock.FACING, Direction.NORTH);
			case 'T' -> CastleBlocks.GRAPPLE_TARGET.defaultBlockState();
			case 'S' -> CastleBlocks.LEVEL_SEVEN_START.defaultBlockState();
			case 'C' -> Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
			case 'o' -> Blocks.DARK_OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, 0);
			default -> Blocks.AIR.defaultBlockState();
		};
	}

	/** Local spot of the start (where you stand, and where falling sends you back to). */
	public static BlockPos startLocal() {
		JsonArray s = data().getAsJsonArray("start");
		return new BlockPos(s.get(0).getAsInt(), s.get(1).getAsInt(), s.get(2).getAsInt());
	}

	/** The player stands at {@code feet} looking {@code forward}: the front door is just ahead and the start floor is level with the ground. */
	public static BlockPos originInFrontOf(BlockPos feet, Direction forward) {
		Direction left = forward.getCounterClockWise();
		return feet.relative(forward, 1).relative(left, width() / 2).below(floor() + 1);
	}

	public static Built build(ServerLevel level, BlockPos origin, Direction forward) {
		JsonObject d = data();
		Rotation rot = CastleLevelSix.rotationFor(forward);
		JsonArray layers = d.getAsJsonArray("layers");
		List<BlockPos> blockEntities = new ArrayList<>();
		for (int pass = 0; pass < 2; pass++) {
			for (int y = 0; y < layers.size(); y++) {
				JsonArray rows = layers.get(y).getAsJsonArray();
				for (int z = 0; z < rows.size(); z++) {
					String row = rows.get(z).getAsString();
					for (int x = 0; x < row.length(); x++) {
						char ch = row.charAt(x);
						boolean attached = "Llo".indexOf(ch) >= 0;
						if (attached != (pass == 1)) {
							continue;
						}
						BlockPos pos = CastleLevelSix.toWorld(origin, forward, x, y, z);
						level.setBlock(pos, block(ch).rotate(rot), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
						if (ch == 'C' || ch == 'o') {
							blockEntities.add(pos);
						}
					}
				}
			}
		}
		JsonObject signs = d.getAsJsonObject("signs");
		for (int y = 0; y < layers.size(); y++) {
			JsonArray rows = layers.get(y).getAsJsonArray();
			for (int z = 0; z < rows.size(); z++) {
				String row = rows.get(z).getAsString();
				for (int x = 0; x < row.length(); x++) {
					char ch = row.charAt(x);
					BlockPos pos = CastleLevelSix.toWorld(origin, forward, x, y, z);
					if (ch == 'C' && level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
						chest.setItem(13, new ItemStack(CastleItems.GRAPPLING_HOOK));
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
		BlockPos s = startLocal();
		return new Built(origin, forward, CastleLevelSix.toWorld(origin, forward, s.getX(), s.getY(), s.getZ()));
	}
}

package com.horrormod.cave;

import java.util.ArrayList;
import java.util.List;

import com.horrormod.castle.CastleLevelSix;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SpeleothemThickness;

/**
 * The STONY CAVE (June and Leon's design, 0.9.0). A rocky cave: the entrance hall, dim and "50 percent scary" (two torches
 * by the door, a few glowing lichen patches, cobwebs and dripstone in the dark corners), where the 5-block-tall Stone
 * Monster guards the Stone Door. Behind the door a corridor leads to three themed rooms: SNOWY (snow, ice, spruce),
 * JUNGLE (jungle leaves, vines, moss) and DESERT (sand, sandstone, cactus), each with a little reward chest.
 *
 * Local x = right, y = up, z = forward, written "facing north" and turned to face your way (same as the castle levels).
 */
public final class StonyCave {
	public static final int WIDTH = 21, HEIGHT = 9, LENGTH = 29;
	/** Hall interior (x, y, z ranges, inclusive). */
	public static final int HALL_X0 = 3, HALL_X1 = 17, HALL_Y1 = 7, HALL_Z0 = 1, HALL_Z1 = 13;
	public static final int DOOR_Z = 14, DOOR_X0 = 9, DOOR_X1 = 11, DOOR_Y1 = 3;
	public static final int ROOM_Z0 = 18, ROOM_Z1 = 27, ROOM_Y1 = 5;
	public static final int[] SNOWY_X = {1, 5}, JUNGLE_X = {8, 12}, DESERT_X = {15, 19};
	public static final BlockPos MONSTER_LOCAL = new BlockPos(10, 1, 8);
	public static final BlockPos SNOWY_CHEST = new BlockPos(3, 1, 26), JUNGLE_CHEST = new BlockPos(10, 1, 26), DESERT_CHEST = new BlockPos(17, 2, 26);

	public record Built(BlockPos origin, Direction forward, StoneMonster monster, List<BlockPos> door, List<BlockPos> chests) {
	}

	private StonyCave() {
	}

	/** The player stands at {@code feet} looking {@code forward}: the cave mouth is just ahead, floor level with the ground. */
	public static BlockPos originInFrontOf(BlockPos feet, Direction forward) {
		Direction left = forward.getCounterClockWise();
		return feet.relative(forward, 1).relative(left, WIDTH / 2).below();
	}

	public static BlockPos toWorld(BlockPos origin, Direction forward, int x, int y, int z) {
		return CastleLevelSix.toWorld(origin, forward, x, y, z);
	}

	private static int noise(int x, int y, int z) {
		int h = x * 73856093 ^ y * 19349663 ^ z * 83492791;
		h ^= h >>> 13;
		h *= 0x5bd1e995;
		return (h ^ h >>> 15) & 0xff;
	}

	private static boolean in(int v, int a, int b) {
		return v >= a && v <= b;
	}

	/** Rocky wall block, themed near each room. */
	private static BlockState shell(int x, int y, int z) {
		int n = noise(x, y, z);
		if (z >= 16 && x <= 6) { // snowy side
			return n < 90 ? Blocks.PACKED_ICE.defaultBlockState() : n < 150 ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.STONE.defaultBlockState();
		}
		if (z >= 16 && x >= 14) { // desert side
			return n < 150 ? Blocks.SANDSTONE.defaultBlockState() : n < 210 ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.CUT_SANDSTONE.defaultBlockState();
		}
		if (z >= 16 && in(x, 7, 13)) { // jungle side
			return n < 100 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : n < 150 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.STONE.defaultBlockState();
		}
		if (n < 120) {
			return Blocks.STONE.defaultBlockState();
		}
		if (n < 170) {
			return Blocks.COBBLESTONE.defaultBlockState();
		}
		if (n < 210) {
			return Blocks.ANDESITE.defaultBlockState();
		}
		if (n < 240) {
			return Blocks.TUFF.defaultBlockState();
		}
		return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
	}

	/** True where the cave is hollow (air). */
	public static boolean isOpen(int x, int y, int z) {
		if (y < 1) {
			return false;
		}
		if (z == 0 && in(x, 9, 11) && y <= 3) {
			return true; // cave mouth
		}
		if (in(x, HALL_X0, HALL_X1) && y <= HALL_Y1 && in(z, HALL_Z0, HALL_Z1)) {
			// round off the top edges a little so it feels like a cave, not a box
			boolean edge = (x == HALL_X0 || x == HALL_X1) && y == HALL_Y1;
			boolean frontBack = (z == HALL_Z0 || z == HALL_Z1) && y == HALL_Y1 && noise(x, y, z) < 128;
			return !edge && !frontBack;
		}
		if (in(x, DOOR_X0, DOOR_X1) && y <= DOOR_Y1 && in(z, DOOR_Z, 15)) {
			return true; // door + short passage
		}
		if (in(x, 1, 19) && y <= 3 && in(z, 16, 17)) {
			return true; // cross corridor to the three rooms
		}
		if (y <= ROOM_Y1 && in(z, ROOM_Z0, ROOM_Z1)) {
			return in(x, SNOWY_X[0], SNOWY_X[1]) || in(x, JUNGLE_X[0], JUNGLE_X[1]) || in(x, DESERT_X[0], DESERT_X[1]);
		}
		return false;
	}

	public static Built build(ServerLevel level, BlockPos origin, Direction forward) {
		Rotation rot = CastleLevelSix.rotationFor(forward);
		Placer p = new Placer(level, origin, forward, rot);
		// 1. the rock and the hollow
		for (int y = 0; y < HEIGHT; y++) {
			for (int z = 0; z < LENGTH; z++) {
				for (int x = 0; x < WIDTH; x++) {
					BlockState s = isOpen(x, y, z) ? Blocks.AIR.defaultBlockState() : shell(x, y, z);
					if (y == 0 && in(x, HALL_X0, HALL_X1) && in(z, HALL_Z0, HALL_Z1) && noise(x, 0, z) > 215) {
						s = Blocks.GRAVEL.defaultBlockState();
					}
					p.set(x, y, z, s);
				}
			}
		}
		// 2. the Stone Door
		List<BlockPos> door = new ArrayList<>();
		for (int y = 1; y <= DOOR_Y1; y++) {
			for (int x = DOOR_X0; x <= DOOR_X1; x++) {
				door.add(p.set(x, y, DOOR_Z, CaveBlocks.STONE_DOOR.defaultBlockState()));
			}
		}
		// 3. the entrance hall: dim and spooky (only two torches, by the cave mouth)
		p.set(8, 1, 2, Blocks.TORCH.defaultBlockState());
		p.set(12, 1, 2, Blocks.TORCH.defaultBlockState());
		BlockState lichen = Blocks.GLOW_LICHEN.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true);
		for (int[] l : new int[][] {{5, 6}, {15, 6}, {5, 11}, {15, 11}, {10, 12}, {4, 2}, {16, 2}, {10, 15}, {3, 17}, {17, 17}, {7, 16}, {13, 16}}) {
			p.set(l[0], 1, l[1], lichen);
		}
		BlockState web = Blocks.COBWEB.defaultBlockState();
		for (int[] c : new int[][] {{3, 6, 1}, {4, 6, 1}, {3, 5, 1}, {17, 6, 1}, {16, 6, 1}, {17, 5, 1}, {3, 6, 13}, {4, 6, 13}, {3, 5, 13}, {3, 6, 12},
				{17, 6, 13}, {16, 6, 13}, {17, 5, 13}, {17, 6, 12}, {3, 1, 13}, {17, 1, 13}, {1, 3, 16}, {19, 3, 16}}) {
			p.set(c[0], c[1], c[2], web);
		}
		BlockState drip = Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN)
				.setValue(PointedDripstoneBlock.THICKNESS, SpeleothemThickness.TIP);
		for (int[] d : new int[][] {{6, 4}, {14, 4}, {7, 10}, {13, 10}, {10, 3}, {5, 8}, {15, 8}}) {
			p.set(d[0], HALL_Y1, d[1], drip);
		}
		p.sign(7, 1, 1, "STONY CAVE", "Beware the", "Stone Monster!", "");
		p.sign(8, 1, 13, "STONE DOOR", "Only the", "Stone Key", "opens it");
		// 4. the three rooms
		List<BlockPos> chests = new ArrayList<>();
		snowyRoom(p);
		jungleRoom(p);
		desertRoom(p);
		chests.add(p.chest(SNOWY_CHEST, new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.SNOWBALL, 16), new ItemStack(Items.COOKIE, 3)));
		chests.add(p.chest(JUNGLE_CHEST, new ItemStack(Items.EMERALD, 3), new ItemStack(Items.MELON_SLICE, 8), new ItemStack(Items.APPLE, 3)));
		chests.add(p.chest(DESERT_CHEST, new ItemStack(Items.GOLD_INGOT, 4), new ItemStack(Items.GOLDEN_APPLE, 1), new ItemStack(Items.BREAD, 3)));
		// 5. the guardian
		BlockPos mp = toWorld(origin, forward, MONSTER_LOCAL.getX(), MONSTER_LOCAL.getY(), MONSTER_LOCAL.getZ());
		StoneMonster monster = CaveEntities.STONE_MONSTER.create(level, EntitySpawnReason.STRUCTURE);
		if (monster != null) {
			monster.snapTo(mp.getX() + 0.5, mp.getY(), mp.getZ() + 0.5, rot.rotate(Direction.SOUTH).toYRot(), 0);
			monster.setYHeadRot(monster.getYRot());
			monster.setPersistenceRequired();
			monster.setHomeTo(mp, 6);
			level.addFreshEntity(monster);
		}
		return new Built(origin, forward, monster, door, chests);
	}

	private static void pillars(Placer p, int x0, int x1, int y1, BlockState log) {
		for (int y = 1; y <= y1; y++) {
			p.set(x0, y, ROOM_Z0, log);
			p.set(x1, y, ROOM_Z0, log);
			p.set(x0, y, ROOM_Z1, log);
			p.set(x1, y, ROOM_Z1, log);
		}
	}

	private static void leafCeiling(Placer p, int x0, int x1, BlockState leaves) {
		for (int z = ROOM_Z0; z <= ROOM_Z1; z++) {
			for (int x = x0; x <= x1; x++) {
				boolean border = x == x0 || x == x1 || z == ROOM_Z0 || z == ROOM_Z1;
				if (border && noise(x, ROOM_Y1, z) < 200) {
					p.set(x, ROOM_Y1, z, leaves);
				}
			}
		}
	}

	private static void hangingSoulLantern(Placer p, int x, int z) {
		p.set(x, ROOM_Y1, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
	}

	private static void snowyRoom(Placer p) {
		int x0 = SNOWY_X[0], x1 = SNOWY_X[1];
		for (int z = ROOM_Z0; z <= ROOM_Z1; z++) {
			for (int x = x0; x <= x1; x++) {
				int n = noise(x, 0, z);
				p.set(x, 0, z, n < 70 ? Blocks.PACKED_ICE.defaultBlockState() : n < 90 ? Blocks.BLUE_ICE.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState());
			}
		}
		leafCeiling(p, x0, x1, Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
		pillars(p, x0, x1, ROOM_Y1, Blocks.SPRUCE_LOG.defaultBlockState());
		for (int[] s : new int[][] {{2, 20}, {4, 22}, {2, 24}, {4, 25}, {3, 19}}) {
			p.set(s[0], 1, s[1], Blocks.SNOW.defaultBlockState());
		}
		// a little spruce "tree" (log with leaves) in the corner
		p.set(2, 1, 22, Blocks.SPRUCE_LOG.defaultBlockState());
		p.set(2, 2, 22, Blocks.SPRUCE_LOG.defaultBlockState());
		p.set(2, 3, 22, Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
		hangingSoulLantern(p, 3, 23);
	}

	private static void jungleRoom(Placer p) {
		int x0 = JUNGLE_X[0], x1 = JUNGLE_X[1];
		for (int z = ROOM_Z0; z <= ROOM_Z1; z++) {
			for (int x = x0; x <= x1; x++) {
				p.set(x, 0, z, Blocks.MOSS_BLOCK.defaultBlockState());
				if (noise(x, 1, z) < 60 && !(x == 10 && z == JUNGLE_CHEST.getZ())) {
					p.set(x, 1, z, Blocks.MOSS_CARPET.defaultBlockState());
				}
			}
		}
		leafCeiling(p, x0, x1, Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
		pillars(p, x0, x1, ROOM_Y1, Blocks.JUNGLE_LOG.defaultBlockState());
		// vines down both side walls
		for (int z = ROOM_Z0 + 1; z < ROOM_Z1; z += 2) {
			for (int y = 2; y <= 4; y++) {
				p.set(x0, y, z, Blocks.VINE.defaultBlockState().setValue(VineBlock.WEST, true));
				p.set(x1, y, z, Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, true));
			}
		}
		hangingSoulLantern(p, 10, 23);
	}

	private static void desertRoom(Placer p) {
		int x0 = DESERT_X[0], x1 = DESERT_X[1];
		for (int z = ROOM_Z0; z <= ROOM_Z1; z++) {
			for (int x = x0; x <= x1; x++) {
				p.set(x, 0, z, Blocks.SANDSTONE.defaultBlockState());
				p.set(x, 1, z, Blocks.SAND.defaultBlockState()); // sand on sandstone, so it never falls
			}
		}
		pillars(p, x0, x1, ROOM_Y1, Blocks.CHISELED_SANDSTONE.defaultBlockState());
		for (int[] c : new int[][] {{16, 20}, {18, 23}, {16, 24}}) {
			p.set(c[0], 2, c[1], Blocks.CACTUS.defaultBlockState());
			p.set(c[0], 3, c[1], Blocks.CACTUS.defaultBlockState());
		}
		for (int[] d : new int[][] {{18, 19}, {17, 21}, {18, 26}}) {
			p.set(d[0], 2, d[1], Blocks.DEAD_BUSH.defaultBlockState());
		}
		hangingSoulLantern(p, 17, 22);
	}

	/** Places blocks in cave-local coordinates. */
	private record Placer(ServerLevel level, BlockPos origin, Direction forward, Rotation rot) {
		BlockPos set(int x, int y, int z, BlockState state) {
			BlockPos pos = toWorld(this.origin, this.forward, x, y, z);
			this.level.setBlock(pos, state.rotate(this.rot), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
			return pos;
		}

		BlockPos chest(BlockPos local, ItemStack... loot) {
			BlockPos pos = this.set(local.getX(), local.getY(), local.getZ(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH));
			if (this.level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
				int slot = 11;
				for (ItemStack s : loot) {
					chest.setItem(slot++, s);
				}
				chest.setChanged();
			}
			return pos;
		}

		void sign(int x, int y, int z, String... lines) {
			BlockPos pos = this.set(x, y, z, Blocks.DARK_OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, 0));
			if (this.level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
				List<Component> msg = new ArrayList<>();
				for (int i = 0; i < 4; i++) {
					msg.add(Component.literal(i < lines.length ? lines[i] : ""));
				}
				sign.setText(new SignText(msg, msg, DyeColor.WHITE, true), SignTextSlot.FRONT);
				sign.setWaxed(true);
				sign.setChanged();
				this.level.sendBlockUpdated(pos, sign.getBlockState(), sign.getBlockState(), Block.UPDATE_CLIENTS);
			}
		}
	}

	/** For tests: what's in a chest is a reward (not empty). */
	public static boolean hasReward(ChestBlockEntity chest) {
		for (int i = 0; i < chest.getContainerSize(); i++) {
			if (!chest.getItem(i).isEmpty()) {
				return true;
			}
		}
		return false;
	}

}

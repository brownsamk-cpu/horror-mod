package com.horrormod.test;

import java.util.List;

import com.horrormod.castle.CastleBlocks;
import com.horrormod.castle.CastleItems;
import com.horrormod.castle.CastleLevelNine;
import com.horrormod.castle.CastleLevelSix;
import com.horrormod.castle.HiddenButtonBlock;
import com.horrormod.castle.PuzzleLampBlock;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Horror Mod 0.8.0: Castle Level 9 (June's find-the-hidden-buttons puzzle). */
public class CastleLevelNineGameTest {
	private static AABB room(BlockPos origin, Direction fwd) {
		return AABB.encapsulatingFullBlocks(CastleLevelSix.toWorld(origin, fwd, 0, 0, 0),
				CastleLevelSix.toWorld(origin, fwd, CastleLevelNine.width() - 1, CastleLevelNine.height() - 1, CastleLevelNine.length() - 1));
	}

	private static void clear(ServerLevel level, BlockPos origin, Direction fwd) {
		AABB all = room(origin, fwd);
		level.getEntitiesOfClass(Entity.class, all.inflate(2), e -> !(e instanceof ServerPlayer)).forEach(Entity::discard);
		for (BlockPos p : BlockPos.betweenClosed(CastleLevelSix.toWorld(origin, fwd, 0, 0, 0),
				CastleLevelSix.toWorld(origin, fwd, CastleLevelNine.width() - 1, CastleLevelNine.height() - 1, CastleLevelNine.length() - 1))) {
			level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
		}
	}

	/** The placer builds the room: 5 hidden buttons, 5 dark lamps, a closed secret door, the painting over button 1, the key chest, skeletons, no cats. */
	@GameTest(maxTicks = 40)
	public void placerBuildsLevelNine(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = helper.absolutePos(new BlockPos(0, 1, 0)).above(140);
		Direction fwd = Direction.EAST;
		CastleLevelNine.Built built = CastleLevelNine.build(level, origin, fwd);
		try {
			helper.assertTrue(built.buttons().size() == 5, "5 buttons in the data");
			for (BlockPos b : built.buttons()) {
				BlockState s = level.getBlockState(b);
				helper.assertTrue(s.is(CastleBlocks.HIDDEN_BUTTON) && !s.getValue(HiddenButtonBlock.FOUND) && s.canSurvive(level, b), "hidden button at " + b);
			}
			int lamps = 0;
			for (int x = 5; x <= 9; x++) {
				BlockState s = level.getBlockState(CastleLevelSix.toWorld(origin, fwd, x, 5, 12));
				helper.assertTrue(s.is(CastleBlocks.PUZZLE_LAMP) && !s.getValue(PuzzleLampBlock.LIT), "dark lamp");
				lamps++;
			}
			helper.assertTrue(lamps == 5, "5 lamps");
			helper.assertTrue(level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 7, 2, 12)).is(CastleBlocks.SECRET_DOOR), "secret door closed");
			helper.assertTrue(level.getBlockEntity(CastleLevelSix.toWorld(origin, fwd, 7, 1, 15)) instanceof ChestBlockEntity c
					&& c.getItem(13).is(CastleItems.LEVEL_TEN_KEY), "Level 10 Key in the secret room");
			helper.assertTrue(level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 1, 3, 10)).is(Blocks.LADDER), "ladder up to the loft");
			helper.assertTrue(level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 11, 2, 10)).is(Blocks.POTTED_POPPY), "flower pot");
			helper.assertTrue(built.painting().isAlive() && built.painting().survives()
					&& built.painting().blockPosition().equals(built.buttons().get(0)), "painting hangs over button 1");
			helper.assertTrue(level.getEntitiesOfClass(Painting.class, room(origin, fwd)).size() == 1, "one painting");
			helper.assertTrue(level.getEntitiesOfClass(Cat.class, room(origin, fwd).inflate(4)).isEmpty(), "no cats in Level 9!");
		} finally {
			clear(level, origin, fwd);
		}
		helper.succeed();
	}

	/** June's extra: 2-3 persistent skeletons in the puzzle room, none in the secret room, none standing on a button. */
	@GameTest(maxTicks = 40)
	public void skeletonsGuardThePuzzleRoom(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = helper.absolutePos(new BlockPos(0, 1, 0)).above(140);
		Direction fwd = Direction.SOUTH;
		CastleLevelNine.Built built = CastleLevelNine.build(level, origin, fwd);
		try {
			List<Skeleton> sk = built.skeletons();
			helper.assertTrue(sk.size() >= 2 && sk.size() <= 3, "2-3 skeletons, got " + sk.size());
			AABB puzzle = AABB.encapsulatingFullBlocks(CastleLevelSix.toWorld(origin, fwd, 1, 1, 1), CastleLevelSix.toWorld(origin, fwd, 13, 7, 11));
			AABB secret = AABB.encapsulatingFullBlocks(CastleLevelSix.toWorld(origin, fwd, 1, 1, 13), CastleLevelSix.toWorld(origin, fwd, 13, 7, 15));
			for (Skeleton s : sk) {
				helper.assertTrue(s.isAlive() && s.isPersistenceRequired(), "skeleton stays");
				helper.assertTrue(puzzle.contains(s.position()) && !secret.contains(s.position()), "skeleton in the puzzle room, not the secret room");
				helper.assertTrue(!built.buttons().contains(s.blockPosition()), "skeleton not on a button");
			}
			helper.assertTrue(level.getEntitiesOfClass(Skeleton.class, secret).isEmpty(), "no skeletons in the secret room");
		} finally {
			clear(level, origin, fwd);
		}
		helper.succeed();
	}

	/** Pressing the buttons one by one counts 1..5 of 5, lights a lamp each time, and the 5th opens the secret door. */
	@GameTest(maxTicks = 60)
	@SuppressWarnings("removal")
	public void findingAllButtonsOpensTheSecretDoor(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = helper.absolutePos(new BlockPos(0, 1, 0)).above(140);
		Direction fwd = Direction.NORTH;
		CastleLevelNine.Built built = CastleLevelNine.build(level, origin, fwd);
		try {
			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			BlockPos door = CastleLevelSix.toWorld(origin, fwd, 7, 2, 12);
			for (int i = 0; i < 5; i++) {
				BlockPos b = built.buttons().get(i);
				player.snapTo(b.getX() + 0.5, b.getY(), b.getZ() + 0.5, 0, 0);
				BlockState s = level.getBlockState(b);
				((HiddenButtonBlock) s.getBlock()).press(s, level, b, player);
				helper.assertTrue(level.getBlockState(b).getValue(HiddenButtonBlock.FOUND), "button " + (i + 1) + " found");
				int lit = 0;
				for (int x = 5; x <= 9; x++) {
					if (level.getBlockState(CastleLevelSix.toWorld(origin, fwd, x, 5, 12)).getValue(PuzzleLampBlock.LIT)) {
						lit++;
					}
				}
				helper.assertTrue(lit == i + 1, (i + 1) + " lamps lit, got " + lit);
				helper.assertTrue(level.getBlockState(door).isAir() == (i == 4), "door opens only after all 5");
			}
			// pressing one again doesn't count twice
			var again = com.horrormod.castle.LevelNinePuzzle.pressed(level, built.buttons().get(0), player);
			helper.assertTrue(again.found() == 5 && again.total() == 5, "still 5 of 5");
			helper.assertTrue(level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 6, 1, 12)).isAir()
					&& level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 8, 3, 12)).isAir(), "whole secret door is open");
		} finally {
			clear(level, origin, fwd);
		}
		helper.succeed();
	}
}

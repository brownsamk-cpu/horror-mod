package com.horrormod.test;

import com.horrormod.castle.CastleBlocks;
import com.horrormod.castle.CastleItems;
import com.horrormod.castle.CastleLevelSeven;
import com.horrormod.castle.CastleLevelSix;
import com.horrormod.castle.CastleRides;
import com.horrormod.castle.ElectraCoreBlock;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Horror Mod 0.7.0: Castle Level 7 (June's design). */
public class CastleLevelSevenGameTest {
	/** The placer builds the course: white Electra Cores over the gap, lava, the rescue field, grapple targets, the hook chest, and no cats. */
	@GameTest(maxTicks = 40)
	@SuppressWarnings("removal")
	public void placerBuildsLevelSeven(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = helper.absolutePos(new BlockPos(0, 1, 0)).above(120);
		Direction fwd = Direction.WEST;
		CastleLevelSeven.Built built = CastleLevelSeven.build(level, origin, fwd);
		int f = CastleLevelSeven.floor();
		try {
			var core = level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 6, f, 8));
			helper.assertTrue(core.is(CastleBlocks.ELECTRA_CORE) && core.getValue(ElectraCoreBlock.FACING) == fwd, "Electra Core heads forward");
			helper.assertTrue(level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 2, 3, 10)).is(Blocks.LAVA), "lava below");
			helper.assertTrue(level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 6, 1, 10)).is(CastleBlocks.RESCUE_FIELD), "rescue field at the bottom of the void");
			helper.assertTrue(level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 6, 6, 10)).isAir(), "open void");
			helper.assertTrue(level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 2, f, 11)).is(CastleBlocks.GRAPPLE_TARGET), "grapple target");
			helper.assertTrue(level.getBlockState(built.start().below()).is(CastleBlocks.LEVEL_SEVEN_START), "start pad");
			helper.assertTrue(level.getBlockEntity(CastleLevelSix.toWorld(origin, fwd, 3, f + 1, 2)) instanceof ChestBlockEntity c
					&& c.getItem(13).is(CastleItems.GRAPPLING_HOOK), "grappling hook chest");
			AABB all = AABB.encapsulatingFullBlocks(CastleLevelSix.toWorld(origin, fwd, 0, 0, 0),
					CastleLevelSix.toWorld(origin, fwd, CastleLevelSeven.width() - 1, CastleLevelSeven.height() - 1, CastleLevelSeven.length() - 1));
			helper.assertTrue(level.getEntitiesOfClass(Cat.class, all.inflate(4)).isEmpty(), "no cats in Level 7!");
		} finally {
			for (BlockPos p : BlockPos.betweenClosed(CastleLevelSix.toWorld(origin, fwd, 0, 0, 0),
					CastleLevelSix.toWorld(origin, fwd, CastleLevelSeven.width() - 1, CastleLevelSeven.height() - 1, CastleLevelSeven.length() - 1))) {
				level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
			}
		}
		helper.succeed();
	}

	/** An Electra Core platform slides forward and carries the player standing on it (and turns around at the wall). */
	@GameTest(maxTicks = 200)
	@SuppressWarnings("removal")
	public void electraCoreCarriesPlayer(GameTestHelper helper) {
		var state = CastleBlocks.ELECTRA_CORE.defaultBlockState().setValue(ElectraCoreBlock.FACING, Direction.SOUTH);
		for (int x = 1; x <= 3; x++) {
			helper.setBlock(new BlockPos(x, 1, 0), state);
			helper.setBlock(new BlockPos(x, 1, 1), state);
		}
		helper.setBlock(new BlockPos(2, 1, 7), Blocks.STONE_BRICKS); // the far side: the platform turns around here
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		Vec3 start = helper.absoluteVec(new Vec3(2.5, 2, 0.5));
		player.snapTo(start.x, start.y, start.z, 0, 0);
		helper.succeedWhen(() -> {
			// the platform is now at z 5..6 (touching the wall), and the player rode 5 blocks with it
			helper.assertBlockPresent(CastleBlocks.ELECTRA_CORE, new BlockPos(2, 1, 6));
			helper.assertBlockPresent(Blocks.AIR, new BlockPos(2, 1, 0));
			helper.assertTrue(Math.abs(player.getZ() - (start.z + 5)) < 0.01 && Math.abs(player.getY() - start.y) < 0.01,
					"player should ride along, at " + player.position() + " start " + start);
		});
	}

	/** The Grappling Hook grabs a block you look at (up to 20 away) and pulls you on top of it; you can use it again. */
	@GameTest(maxTicks = 100)
	@SuppressWarnings("removal")
	public void grapplingHookPullsPlayer(GameTestHelper helper) {
		helper.setBlock(new BlockPos(2, 2, 7), CastleBlocks.GRAPPLE_TARGET);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		CastleRides.track(player);
		Vec3 start = helper.absoluteVec(new Vec3(2.5, 2, 0.5));
		// yaw 0 = looking south (+z); aim at the target's face
		Vec3 eye = start.add(0, player.getEyeHeight(), 0);
		Vec3 aim = helper.absoluteVec(new Vec3(2.5, 2.5, 7.0));
		float yaw = (float) Math.toDegrees(Math.atan2(-(aim.x - eye.x), aim.z - eye.z));
		float pitch = (float) Math.toDegrees(Math.atan2(eye.y - aim.y, Math.hypot(aim.x - eye.x, aim.z - eye.z)));
		player.snapTo(start.x, start.y, start.z, yaw, pitch);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CastleItems.GRAPPLING_HOOK));
		CastleItems.GRAPPLING_HOOK.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(CastleRides.isRiding(player), "the hook should grab the target");
		Vec3 top = helper.absoluteVec(new Vec3(2.5, 3, 7.5));
		helper.succeedWhen(() -> {
			helper.assertFalse(CastleRides.isRiding(player), "still being pulled");
			helper.assertTrue(player.position().distanceTo(top) < 0.6, "should be pulled on top of the target, at " + player.position() + " want " + top);
			helper.assertTrue(player.getMainHandItem().is(CastleItems.GRAPPLING_HOOK), "you keep the hook to use again");
		});
	}

	/** Falling into the void (the rescue field) or touching the lava sends you back to the start pad, with no damage and no lost items. */
	@GameTest(maxTicks = 60)
	@SuppressWarnings("removal")
	public void fallAndLavaTeleportBack(GameTestHelper helper) {
		helper.setBlock(new BlockPos(1, 1, 1), CastleBlocks.LEVEL_SEVEN_START);
		helper.setBlock(new BlockPos(5, 1, 5), CastleBlocks.RESCUE_FIELD);
		helper.setBlock(new BlockPos(5, 2, 5), CastleBlocks.RESCUE_FIELD);
		for (BlockPos rim : new BlockPos[] {new BlockPos(2, 1, 6), new BlockPos(4, 1, 6), new BlockPos(3, 1, 5), new BlockPos(3, 1, 7)}) {
			helper.setBlock(rim, Blocks.STONE_BRICKS); // keep the lava in its little pool
		}
		helper.setBlock(new BlockPos(3, 1, 6), Blocks.LAVA);
		helper.setBlock(new BlockPos(3, 2, 6), CastleBlocks.RESCUE_FIELD);
		ServerPlayer faller = helper.makeMockServerPlayerInLevel();
		ServerPlayer swimmer = helper.makeMockServerPlayerInLevel();
		CastleRides.track(faller);
		CastleRides.track(swimmer);
		faller.getInventory().add(new ItemStack(CastleItems.GRAPPLING_HOOK));
		Vec3 fall = helper.absoluteVec(new Vec3(5.5, 1.2, 5.5));
		Vec3 lava = helper.absoluteVec(new Vec3(3.5, 1.0, 6.5));
		faller.snapTo(fall.x, fall.y, fall.z, 0, 0);
		swimmer.snapTo(lava.x, lava.y, lava.z, 0, 0);
		float health = swimmer.getHealth();
		Vec3 home = helper.absoluteVec(new Vec3(1.5, 2, 1.5));
		helper.succeedWhen(() -> {
			helper.assertTrue(faller.position().distanceTo(home) < 0.1, "faller back at the start, at " + faller.position());
			helper.assertTrue(swimmer.position().distanceTo(home) < 0.1, "lava toucher back at the start, at " + swimmer.position());
			helper.assertTrue(swimmer.getHealth() >= health && !swimmer.isOnFire(), "lava didn't hurt");
			helper.assertTrue(faller.getInventory().contains(new ItemStack(CastleItems.GRAPPLING_HOOK)), "kept items");
		});
	}
}

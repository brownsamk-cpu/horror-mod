package com.horrormod.test;

import com.horrormod.castle.CastleBlocks;
import com.horrormod.castle.CastleItems;
import com.horrormod.castle.CastleLevelSix;
import com.horrormod.castle.CastleRides;
import com.horrormod.castle.StrayBlastPadBlock;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Horror Mod 0.6.0: Castle Level 6. */
public class CastleLevelSixGameTest {
	/** The placer builds the whole course: walls, climbing ledges, vines, the pad, strays, the zip line, both chests and the locked gate. */
	@GameTest(maxTicks = 40)
	@SuppressWarnings("removal")
	public void placerBuildsCourse(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		// build way up in the sky so it doesn't bump into other tests, then clean up
		BlockPos origin = helper.absolutePos(new BlockPos(0, 1, 0)).above(150);
		Direction fwd = Direction.EAST;
		CastleLevelSix.Built built = CastleLevelSix.build(level, origin, fwd);
		try {
			helper.assertTrue(level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 0, 5, 10)).is(Blocks.STONE_BRICKS)
					|| level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 0, 5, 10)).is(Blocks.DEEPSLATE_BRICKS)
					|| level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 0, 5, 10)).is(Blocks.MOSSY_STONE_BRICKS)
					|| level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 0, 5, 10)).is(Blocks.CRACKED_STONE_BRICKS), "castle wall");
			helper.assertTrue(level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 2, 1, 7)).is(Blocks.STONE_BRICK_STAIRS), "climbing ledge");
			helper.assertTrue(level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 6, 11, 9)).is(Blocks.VINE), "vines");
			helper.assertTrue(level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 1, 1, 20)).is(Blocks.WATER), "safe water pool");
			var pad = level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 6, 13, 18));
			helper.assertTrue(pad.is(CastleBlocks.STRAY_BLAST_PAD) && pad.getValue(StrayBlastPadBlock.FACING) == fwd, "blast pad faces forward");
			var chain = level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 6, 16, 30));
			helper.assertTrue(chain.getBlock() instanceof ChainBlock && chain.getValue(ChainBlock.AXIS) == fwd.getAxis(), "zip line rope along the course");
			helper.assertTrue(level.getBlockState(CastleLevelSix.toWorld(origin, fwd, 6, 15, 44)).is(CastleBlocks.LEVEL_SEVEN_GATE), "locked gate");
			helper.assertTrue(level.getBlockEntity(CastleLevelSix.toWorld(origin, fwd, 4, 14, 25)) instanceof ChestBlockEntity c1
					&& c1.getItem(13).is(CastleItems.ZIP_LINE_HOOK), "hook chest");
			helper.assertTrue(level.getBlockEntity(CastleLevelSix.toWorld(origin, fwd, 6, 14, 41)) instanceof ChestBlockEntity c2
					&& c2.getItem(13).is(CastleItems.LEVEL_SEVEN_KEY), "key chest");
			helper.assertTrue(built.strays().size() == 4, "4 strays, got " + built.strays().size());
			for (Stray s : built.strays()) {
				helper.assertTrue(s.isAlive() && s.isPersistenceRequired(), "strays stay around");
			}
		} finally {
			built.strays().forEach(Stray::discard);
			for (BlockPos p : BlockPos.betweenClosed(CastleLevelSix.toWorld(origin, fwd, 0, 0, 0), CastleLevelSix.toWorld(origin, fwd, 12, 20, 49))) {
				level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
			}
		}
		helper.succeed();
	}

	/** Holding the Zip Line Hook near a chain rope slides you all the way to the far end. */
	@GameTest(maxTicks = 80)
	@SuppressWarnings("removal")
	public void zipLineMovesPlayer(GameTestHelper helper) {
		for (int z = 1; z <= 6; z++) {
			helper.setBlock(new BlockPos(1, 4, z), Blocks.IRON_CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.Z));
		}
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		CastleRides.track(player);
		Vec3 start = helper.absoluteVec(new Vec3(1.5, 2, 1.5));
		player.snapTo(start.x, start.y, start.z, 0, 0);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(CastleItems.ZIP_LINE_HOOK));
		CastleItems.ZIP_LINE_HOOK.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
		helper.assertTrue(CastleRides.isRiding(player), "should be riding the zip line");
		double endZ = helper.absoluteVec(new Vec3(1.5, 2, 7.5)).z;
		helper.succeedWhen(() -> {
			helper.assertFalse(CastleRides.isRiding(player), "still riding");
			helper.assertTrue(Math.abs(player.getZ() - endZ) < 0.6, "should arrive past the far end, z=" + player.getZ() + " want " + endZ);
		});
	}

	/** The Level 7 Key opens the whole gate (and the gate stays shut without it). */
	@GameTest(maxTicks = 20)
	@SuppressWarnings("removal")
	public void keyOpensGate(GameTestHelper helper) {
		for (int x = 1; x <= 3; x++) {
			for (int y = 1; y <= 3; y++) {
				helper.setBlock(new BlockPos(x, y, 2), CastleBlocks.LEVEL_SEVEN_GATE);
			}
		}
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		BlockPos gate = helper.absolutePos(new BlockPos(2, 2, 2));
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(gate), Direction.SOUTH, gate, false);
		var state = helper.getLevel().getBlockState(gate);
		state.useItemOn(new ItemStack(net.minecraft.world.item.Items.STICK), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
		helper.assertBlockPresent(CastleBlocks.LEVEL_SEVEN_GATE, new BlockPos(2, 2, 2));
		ItemStack key = new ItemStack(CastleItems.LEVEL_SEVEN_KEY);
		player.setItemInHand(InteractionHand.MAIN_HAND, key);
		state.useItemOn(key, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
		for (int x = 1; x <= 3; x++) {
			for (int y = 1; y <= 3; y++) {
				helper.assertBlockPresent(Blocks.AIR, new BlockPos(x, y, 2));
			}
		}
		helper.succeed();
	}

	/** A stray's arrow hitting you on the Blast Pad throws you forward onto the landing platform (and doesn't hurt). */
	@GameTest(maxTicks = 160)
	@SuppressWarnings("removal")
	public void launchZonePushesTowardPlatform(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		helper.setBlock(new BlockPos(3, 1, 0), CastleBlocks.STRAY_BLAST_PAD.defaultBlockState().setValue(StrayBlastPadBlock.FACING, Direction.SOUTH));
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		CastleRides.track(player);
		Vec3 start = helper.absoluteVec(new Vec3(3.5, 2, 0.5));
		Vec3 waiting = helper.absoluteVec(new Vec3(1.5, 2, 3.5)); // off the pad while waiting
		player.snapTo(waiting.x, waiting.y, waiting.z, 0, 0);
		Stray stray = helper.spawn(EntityTypes.STRAY, new BlockPos(6, 2, 0));
		stray.setNoAi(true); // it's only the arrow's owner here; a real shooting stray could hit other tests
		// new players can't be hurt for their first 3 seconds, so wait that out first
		helper.runAfterDelay(65, () -> {
			player.snapTo(start.x, start.y, start.z, 0, 0);
			float health = player.getHealth();
			Arrow arrow = EntityTypes.ARROW.create(level, EntitySpawnReason.TRIGGERED);
			arrow.snapTo(start.x, start.y + 1, start.z);
			arrow.setOwner(stray);
			arrow.setNoGravity(true);
			level.addFreshEntity(arrow);
			// the hit itself also goes straight to the blast, no damage
			player.hurtServer(level, level.damageSources().arrow(arrow, stray), 3.0F);
			helper.runAfterDelay(2, () -> {
				helper.assertTrue(CastleRides.isRiding(player), "arrow on the pad should blast the player");
				helper.assertTrue(player.getHealth() >= health, "the blast doesn't hurt");
				stray.discard();
			});
		});
		Vec3 target = helper.absoluteVec(new Vec3(3.5, 2, 0.5 + CastleLevelSix.padJump()));
		helper.succeedWhen(() -> {
			helper.assertTrue(stray.isRemoved(), "not hit yet");
			helper.assertFalse(CastleRides.isRiding(player), "still flying");
			helper.assertTrue(player.position().distanceTo(target) < 0.8, "should land on the platform, at " + player.position() + " want " + target);
		});
	}
}

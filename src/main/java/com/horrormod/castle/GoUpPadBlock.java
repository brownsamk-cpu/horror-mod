package com.horrormod.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** 0.7.1: the Go Up Pad at the end of Level 6. Step on it and you whoosh UP to Level 7's start pad, stacked above. */
public class GoUpPadBlock extends Block {
	/** How far up / sideways to look for Level 7's start pad. */
	public static final int UP = 48, SIDE = 16, ALONG = 56;

	public GoUpPadBlock(Properties properties) {
		super(properties);
	}

	/** The nearest Level 7 start pad ABOVE this pad, or null. */
	public static BlockPos findStartAbove(ServerLevel level, BlockPos pad) {
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (BlockPos p : BlockPos.betweenClosed(pad.offset(-ALONG, 2, -ALONG), pad.offset(ALONG, UP, ALONG))) {
			if (level.getBlockState(p).is(CastleBlocks.LEVEL_SEVEN_START)) {
				double d = p.distSqr(pad);
				if (d < bestDist) {
					bestDist = d;
					best = p.immutable();
				}
			}
		}
		return best;
	}

	public static boolean goUp(ServerPlayer player, BlockPos pad) {
		ServerLevel level = (ServerLevel) player.level();
		BlockPos start = findStartAbove(level, pad);
		if (start == null) {
			player.sendOverlayMessage(Component.translatable("block.horrormod.go_up_pad.none"));
			return false;
		}
		Vec3 to = Vec3.atBottomCenterOf(start.above());
		CastleRides.stop(player);
		player.teleportTo(to.x, to.y, to.z);
		player.setDeltaMovement(Vec3.ZERO);
		player.resetFallDistance();
		level.sendParticles(ParticleTypes.END_ROD, to.x, to.y + 1, to.z, 30, 0.4, 0.8, 0.4, 0.05);
		level.playSound(null, start, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.6F);
		player.sendOverlayMessage(Component.translatable("block.horrormod.go_up_pad.whoosh"));
		return true;
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (entity instanceof ServerPlayer player && !player.isSpectator()) {
			goUp(player, pos);
		}
		super.stepOn(level, pos, state, entity);
	}
}

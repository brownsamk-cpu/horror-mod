package com.horrormod.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Kid-friendly falling in Level 7: lava or the void never hurts, it just sends you back to the start pad. */
public final class LevelSevenRescue {
	/** How far (sideways) to look for the start pad. */
	public static final int SEARCH = 48;

	private LevelSevenRescue() {
	}

	/** Is the player in (or within {@code around} blocks of) the Level 7 rescue field? */
	public static boolean inDanger(ServerPlayer player, double around) {
		ServerLevel level = (ServerLevel) player.level();
		var box = player.getBoundingBox().inflate(around);
		for (BlockPos p : BlockPos.betweenClosed(BlockPos.containing(box.getMinPosition()), BlockPos.containing(box.getMaxPosition()))) {
			if (level.getBlockState(p).is(CastleBlocks.RESCUE_FIELD)) {
				return true;
			}
		}
		return false;
	}

	public static BlockPos findStart(ServerLevel level, BlockPos near) {
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (BlockPos p : BlockPos.betweenClosed(near.offset(-SEARCH, -6, -SEARCH), near.offset(SEARCH, 24, SEARCH))) {
			if (level.getBlockState(p).is(CastleBlocks.LEVEL_SEVEN_START)) {
				double d = p.distSqr(near);
				if (d < bestDist) {
					bestDist = d;
					best = p.immutable();
				}
			}
		}
		return best;
	}

	/** Sends the player back to the nearest start pad. Returns false if there's no pad around. */
	public static boolean rescue(ServerPlayer player) {
		ServerLevel level = (ServerLevel) player.level();
		BlockPos start = findStart(level, player.blockPosition());
		if (start == null) {
			return false;
		}
		Vec3 to = Vec3.atBottomCenterOf(start.above());
		CastleRides.stop(player);
		player.teleportTo(to.x, to.y, to.z);
		player.setDeltaMovement(Vec3.ZERO);
		player.resetFallDistance();
		player.clearFire();
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, to.x, to.y + 1, to.z, 30, 0.4, 0.6, 0.4, 0.1);
		level.playSound(null, start, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.4F);
		player.sendOverlayMessage(Component.translatable("block.horrormod.level_seven_start.whoosh"));
		return true;
	}
}

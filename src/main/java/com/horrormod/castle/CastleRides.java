package com.horrormod.castle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Moves players along a path, a little bit every tick: used by the zip line (straight across under the rope)
 * and the Stray Blast Pad (a big arc forward onto the landing platform). Nobody gets fall damage from a ride.
 */
public final class CastleRides {
	/** A ride from {@code from} to {@code to} taking {@code ticks}; {@code arc} = how high it bumps up in the middle. */
	public record Ride(Vec3 from, Vec3 to, int ticks, double arc) {
		Vec3 at(int tick) {
			double t = Math.min(1.0, tick / (double) ticks);
			return from.lerp(to, t).add(0, arc * 4 * t * (1 - t), 0);
		}
	}

	private static final Map<UUID, Ride> RIDES = new HashMap<>();
	private static final Map<UUID, Integer> RIDE_TICK = new HashMap<>();
	private static final Map<UUID, Integer> PAD_WAIT = new HashMap<>();
	/** Standing on the Blast Pad this long without being hit also launches you (so it works on Peaceful too). */
	public static final int PAD_WAIT_TICKS = 60;
	public static final double ZIP_REACH = 3.0;
	public static final double ZIP_SPEED = 0.6;

	private CastleRides() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(CastleRides::tick);
		// a stray's arrow hitting you on the Blast Pad doesn't hurt: it BLASTS you across the gap
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayer player && source.getDirectEntity() instanceof AbstractArrow && padUnder(player) != null) {
				launchFromPad(player);
				return false;
			}
			return true;
		});
	}

	public static boolean isRiding(ServerPlayer player) {
		return RIDES.containsKey(player.getUUID());
	}

	public static void start(ServerPlayer player, Ride ride) {
		RIDES.put(player.getUUID(), ride);
		RIDE_TICK.put(player.getUUID(), 0);
		player.resetFallDistance();
	}

	/** The Blast Pad under the player's feet, or null. */
	public static BlockPos padUnder(ServerPlayer player) {
		BlockPos below = BlockPos.containing(player.getX(), player.getY() - 0.3, player.getZ());
		return player.level().getBlockState(below).is(CastleBlocks.STRAY_BLAST_PAD) ? below : null;
	}

	/** Throws the player from the pad, in the pad's arrow direction, onto the landing platform. */
	public static boolean launchFromPad(ServerPlayer player) {
		BlockPos pad = padUnder(player);
		if (pad == null || isRiding(player)) {
			return false;
		}
		Direction facing = player.level().getBlockState(pad).getValue(StrayBlastPadBlock.FACING);
		Vec3 to = Vec3.atBottomCenterOf(pad.relative(facing, CastleLevelSix.padJump()).above());
		start(player, new Ride(player.position(), to, 26, 4.0));
		ServerLevel level = (ServerLevel) player.level();
		level.playSound(null, pad, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0F, 0.8F);
		level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY(), player.getZ(), 15, 0.4, 0.2, 0.4, 0.05);
		return true;
	}

	/** Finds the chain rope near the player and starts sliding to its far end. */
	public static boolean startZipLine(ServerPlayer player) {
		if (isRiding(player)) {
			return false;
		}
		ServerLevel level = (ServerLevel) player.level();
		Vec3 eyes = player.getEyePosition();
		BlockPos best = null;
		double bestDist = ZIP_REACH * ZIP_REACH;
		int r = (int) Math.ceil(ZIP_REACH);
		BlockPos center = BlockPos.containing(eyes);
		for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
			BlockState s = level.getBlockState(p);
			if (s.getBlock() instanceof ChainBlock && s.getValue(ChainBlock.AXIS).isHorizontal()) {
				double dist = Vec3.atCenterOf(p).distanceToSqr(eyes);
				if (dist <= bestDist) {
					bestDist = dist;
					best = p.immutable();
				}
			}
		}
		if (best == null) {
			return false;
		}
		Direction.Axis axis = level.getBlockState(best).getValue(ChainBlock.AXIS);
		Direction plus = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
		BlockPos a = walk(level, best, plus);
		BlockPos b = walk(level, best, plus.getOpposite());
		// ride to whichever end is farther away
		boolean toA = Vec3.atCenterOf(a).distanceToSqr(player.position()) >= Vec3.atCenterOf(b).distanceToSqr(player.position());
		BlockPos end = toA ? a : b;
		Direction dir = toA ? plus : plus.getOpposite();
		Vec3 to = Vec3.atBottomCenterOf(end.relative(dir, 1).below(2));
		int ticks = Math.max(5, (int) Math.ceil(player.position().distanceTo(to) / ZIP_SPEED));
		start(player, new Ride(player.position(), to, ticks, 0.0));
		level.playSound(null, player.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.0F, 1.4F);
		return true;
	}

	private static BlockPos walk(ServerLevel level, BlockPos from, Direction dir) {
		BlockPos p = from;
		for (int i = 0; i < 256 && level.getBlockState(p.relative(dir)).getBlock() instanceof ChainBlock; i++) {
			p = p.relative(dir);
		}
		return p;
	}

	private static void tick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			tickPlayer(player);
		}
		// mock players in game tests aren't in the player list; tick them too
		RIDES.keySet().removeIf(id -> server.getPlayerList().getPlayer(id) == null && TEST_PLAYERS.get(id) == null);
		for (ServerPlayer p : TEST_PLAYERS.values().toArray(new ServerPlayer[0])) {
			if (server.getPlayerList().getPlayer(p.getUUID()) == null) {
				tickPlayer(p);
			}
		}
	}

	private static final Map<UUID, ServerPlayer> TEST_PLAYERS = new HashMap<>();

	/** For game tests: also move this (mock) player around. */
	public static void track(ServerPlayer player) {
		TEST_PLAYERS.put(player.getUUID(), player);
	}

	private static void tickPlayer(ServerPlayer player) {
		UUID id = player.getUUID();
		Ride ride = RIDES.get(id);
		if (ride != null) {
			int t = RIDE_TICK.merge(id, 1, Integer::sum);
			Vec3 pos = ride.at(t);
			player.teleportTo(pos.x, pos.y, pos.z);
			player.setDeltaMovement(Vec3.ZERO);
			player.resetFallDistance();
			if (t % 3 == 0) {
				((ServerLevel) player.level()).sendParticles(ParticleTypes.CLOUD, pos.x, pos.y + 1.0, pos.z, 1, 0.1, 0.1, 0.1, 0.0);
			}
			if (t >= ride.ticks()) {
				RIDES.remove(id);
				RIDE_TICK.remove(id);
				PAD_WAIT.remove(id);
			}
			return;
		}
		if (padUnder(player) != null && !player.isSpectator()) {
			// a stray's arrow reaching you on the pad: blast off! (works on every difficulty, and the arrow never hurts)
			for (AbstractArrow arrow : player.level().getEntitiesOfClass(AbstractArrow.class, player.getBoundingBox().inflate(0.4),
					a -> a.getOwner() instanceof Stray)) {
				arrow.discard();
				launchFromPad(player);
				return;
			}
			int w = PAD_WAIT.merge(id, 1, Integer::sum);
			if (w >= PAD_WAIT_TICKS) {
				PAD_WAIT.remove(id);
				launchFromPad(player);
			}
		} else {
			PAD_WAIT.remove(id);
		}
	}
}

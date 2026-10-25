package com.horrormod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** The 4 custom status effects. All mildly annoying and kid-fun, none of them hurt. */
public final class MysteriousEffects {
	private MysteriousEffects() {
	}

	/** Bouncy: every time you land, you bounce back up. */
	public static class Bouncy extends MobEffect {
		public static final double BOUNCE = 0.55;

		Bouncy(final int color) {
			super(MobEffectCategory.NEUTRAL, color);
		}

		@Override
		public boolean shouldApplyEffectTickThisTick(final int tickCount, final int amplification) {
			return true;
		}

		@Override
		public boolean applyEffectTick(final ServerLevel level, final LivingEntity mob, final int amplification) {
			if (mob.onGround() && !mob.isShiftKeyDown()) {
				Vec3 v = mob.getDeltaMovement();
				mob.setDeltaMovement(v.x, BOUNCE + 0.1 * amplification, v.z);
				mob.needsSync = true;
				if (mob instanceof net.minecraft.server.level.ServerPlayer sp && sp.connection != null) {
					sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(sp)); // players move client-side
				}
				mob.resetFallDistance();
				level.sendParticles(ParticleTypes.ITEM_SLIME, mob.getX(), mob.getY() + 0.1, mob.getZ(), 4, 0.25, 0.0, 0.25, 0.0);
			}
			return true;
		}
	}

	/** Glow: you glow (outline) and leave a trail of sparkles. */
	public static class Glow extends MobEffect {
		Glow(final int color) {
			super(MobEffectCategory.NEUTRAL, color);
		}

		@Override
		public boolean shouldApplyEffectTickThisTick(final int tickCount, final int amplification) {
			return true;
		}

		@Override
		public boolean applyEffectTick(final ServerLevel level, final LivingEntity mob, final int amplification) {
			if (!mob.hasEffect(MobEffects.GLOWING) || mob.getEffect(MobEffects.GLOWING).getDuration() < 5) {
				mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, true, false, false)); // the outline only, hidden
			}
			if (mob.tickCount % 2 == 0) {
				level.sendParticles(ParticleTypes.END_ROD, mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 2, 0.3, 0.5, 0.3, 0.01);
				level.sendParticles(ParticleTypes.ELECTRIC_SPARK, mob.getX(), mob.getY() + 0.1, mob.getZ(), 1, 0.2, 0.0, 0.2, 0.0);
			}
			return true;
		}
	}

	/** Bat: little harmless bats swirl around you (the spooky screen tint is drawn by the client). */
	public static class Bat extends MobEffect {
		Bat(final int color) {
			super(MobEffectCategory.NEUTRAL, color);
		}

		@Override
		public boolean shouldApplyEffectTickThisTick(final int tickCount, final int amplification) {
			return tickCount % 2 == 0;
		}

		@Override
		public boolean applyEffectTick(final ServerLevel level, final LivingEntity mob, final int amplification) {
			double t = mob.tickCount * 0.25;
			for (int i = 0; i < 3; i++) {
				double a = t + i * (Math.PI * 2 / 3);
				double r = 0.9;
				double x = mob.getX() + Math.cos(a) * r, z = mob.getZ() + Math.sin(a) * r;
				double y = mob.getY() + 0.6 + 0.5 * Math.sin(t * 0.7 + i) + mob.getBbHeight() * 0.3;
				// count 0 = the dx/dy/dz values are the bat's velocity: a little push around the circle
				level.sendParticles(ModParticles.BAT, x, y, z, 0, -Math.sin(a) * 0.15, 0.02, Math.cos(a) * 0.15, 1.0);
			}
			return true;
		}
	}

	/** Slime Feet: you slide around like you're on ice (players: done on the client, see HorrorModClient). */
	public static class SlimeFeet extends MobEffect {
		public static final double SLIDE = 1.55;
		public static final double MAX_SPEED = 0.55;

		SlimeFeet(final int color) {
			super(MobEffectCategory.NEUTRAL, color);
		}

		@Override
		public boolean shouldApplyEffectTickThisTick(final int tickCount, final int amplification) {
			return true;
		}

		@Override
		public boolean applyEffectTick(final ServerLevel level, final LivingEntity mob, final int amplification) {
			if (!(mob instanceof Player)) {
				slide(mob);
			}
			if (mob.onGround() && mob.tickCount % 4 == 0) {
				level.sendParticles(ParticleTypes.ITEM_SLIME, mob.getX(), mob.getY() + 0.05, mob.getZ(), 1, 0.2, 0.0, 0.2, 0.0);
			}
			return true;
		}

		/** Keeps most of your sideways speed while on the ground, like ice. */
		public static void slide(final LivingEntity mob) {
			if (!mob.onGround()) {
				return;
			}
			Vec3 v = mob.getDeltaMovement();
			double hx = v.x * SLIDE, hz = v.z * SLIDE;
			double speed = Math.sqrt(hx * hx + hz * hz);
			if (speed > MAX_SPEED) {
				hx *= MAX_SPEED / speed;
				hz *= MAX_SPEED / speed;
			}
			mob.setDeltaMovement(hx, v.y, hz);
		}
	}
}

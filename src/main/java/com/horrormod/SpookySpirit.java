package com.horrormod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Spooky Spirit (horrormod:spooky_spirit): a small, clear, see-through ghost. Spooky-fun, never scary or harmful:
 * it has no gravity, floats slowly and drifts around, bobs gently up and down, looks at players, and now and then
 * says a soft "wooo". It never attacks or hurts anyone. It spawns rarely, only in the dark (at night or in caves).
 */
public class SpookySpirit extends PathfinderMob {
	public static final int WOOO_INTERVAL = 200; // about every 10 s on average
	private int wooos;

	public SpookySpirit(final EntityType<? extends SpookySpirit> type, final Level level) {
		super(type, level);
		this.moveControl = new FlyingMoveControl<>(this, 10, true);
		this.setNoGravity(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 10.0)
				.add(Attributes.FLYING_SPEED, 0.12F)
				.add(Attributes.MOVEMENT_SPEED, 0.08F);
	}

	public static boolean checkSpiritSpawnRules(final EntityType<SpookySpirit> type, final ServerLevelAccessor level, final EntitySpawnReason reason,
			final BlockPos pos, final RandomSource random) {
		return Monster.isDarkEnoughToSpawn(level, pos, random) && Mob.checkMobSpawnRules(type, level, reason, pos, random);
	}

	@Override
	protected PathNavigation createNavigation(final Level level) {
		FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
		nav.setCanOpenDoors(false);
		nav.setCanFloat(true);
		return nav;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 0.6));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		// no target goals at all: the spirit never attacks anyone
	}

	@Override
	public void travel(final Vec3 input) {
		this.travelFlying(input, this.getSpeed());
	}

	@Override
	public void aiStep() {
		super.aiStep();
		this.setNoGravity(true);
		// a gentle hover: drift up and down a tiny bit
		if (!this.isNoAi()) {
			double bob = Math.sin(this.tickCount * 0.08) * 0.004;
			this.setDeltaMovement(this.getDeltaMovement().add(0.0, bob, 0.0));
		}
	}

	@Override
	public int getAmbientSoundInterval() {
		return WOOO_INTERVAL;
	}

	@Override
	public void playAmbientSound() {
		super.playAmbientSound();
		this.wooos++;
	}

	public int getWooos() {
		return this.wooos;
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return ModSounds.SPOOKY_SPIRIT_WOOO;
	}

	@Override
	protected float getSoundVolume() {
		return 0.6F;
	}

	@Override
	protected SoundEvent getHurtSound(final DamageSource source) {
		return SoundEvents.ALLAY_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ALLAY_DEATH;
	}

	@Override
	public boolean doHurtTarget(final ServerLevel level, final Entity target) {
		return false; // passive: never deals damage
	}

	@Override
	public boolean causeFallDamage(final double fallDistance, final float damageModifier, final DamageSource damageSource) {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(final double distSqr) {
		return !this.hasCustomName() && !this.isPersistenceRequired();
	}
}

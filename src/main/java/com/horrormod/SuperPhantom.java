package com.horrormod;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Super Phantom (horrormod:super_phantom): "like a phantom, but worse". Bigger and darker than a vanilla phantom,
 * with glowing eyes. It flies and swoops slowly around and NEVER does damage. Its trick: when it swoops within about
 * 2 blocks of a survival/adventure player it snatches ONE item (count 1) from a random non-empty main-inventory or
 * hotbar slot (never worn armor or the offhand), then waits about 30 seconds before it can snatch again.
 * It keeps everything it stole and drops ALL of it when it dies, so you can get your things back.
 */
public class SuperPhantom extends PathfinderMob {
	public static final int SNATCH_COOLDOWN = 600; // 30 s
	public static final double SNATCH_RANGE = 2.0;
	private final SimpleContainer stolen = new SimpleContainer(27);
	private int cooldown;

	public SuperPhantom(final EntityType<? extends SuperPhantom> type, final Level level) {
		super(type, level);
		this.moveControl = new FlyingMoveControl<>(this, 10, true);
		this.setNoGravity(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0)
				.add(Attributes.FLYING_SPEED, 0.25F)
				.add(Attributes.MOVEMENT_SPEED, 0.12F)
				.add(Attributes.FOLLOW_RANGE, 16.0);
	}

	/** Rare, only at night (dark outside), only where it can see the sky. */
	public static boolean checkSuperPhantomSpawnRules(final EntityType<SuperPhantom> type, final ServerLevelAccessor level, final EntitySpawnReason reason,
			final BlockPos pos, final RandomSource random) {
		if (EntitySpawnReason.isSpawner(reason)) {
			return true;
		}
		return level.getLevel().isDarkOutside() && level.canSeeSky(pos) && random.nextInt(4) == 0
				&& Mob.checkMobSpawnRules(type, level, reason, pos, random);
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
		this.goalSelector.addGoal(3, new SwoopGoal());
		this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 0.8));
		this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		// no target goals: it never attacks anyone
	}

	@Override
	public void travel(final Vec3 input) {
		this.travelFlying(input, this.getSpeed());
	}

	@Override
	public void aiStep() {
		super.aiStep();
		this.setNoGravity(true);
		if (this.level() instanceof ServerLevel level) {
			if (this.cooldown > 0) {
				this.cooldown--;
			} else {
				Player target = this.findSnatchable(SNATCH_RANGE);
				if (target != null) {
					this.trySnatch(level, target);
				}
			}
		}
	}

	private static boolean canBeSnatchedFrom(final Player p) {
		return p.isAlive() && !p.isCreative() && !p.isSpectator();
	}

	private @Nullable Player findSnatchable(final double range) {
		Player best = null;
		double bestDist = range * range;
		for (Player p : this.level().players()) {
			double d = p.distanceToSqr(this);
			if (canBeSnatchedFrom(p) && d <= bestDist) {
				best = p;
				bestDist = d;
			}
		}
		return best;
	}

	/** Snatches one item from a random non-empty main inventory / hotbar slot. Returns what it took (or EMPTY). */
	public ItemStack trySnatch(final ServerLevel level, final Player player) {
		if (this.cooldown > 0 || !canBeSnatchedFrom(player)) {
			return ItemStack.EMPTY;
		}
		NonNullList<ItemStack> items = player.getInventory().getNonEquipmentItems(); // 36 slots: hotbar + main, no armor/offhand
		List<Integer> slots = new ArrayList<>();
		for (int i = 0; i < items.size(); i++) {
			if (!items.get(i).isEmpty() && this.stolen.canAddItem(items.get(i).copyWithCount(1))) {
				slots.add(i);
			}
		}
		if (slots.isEmpty()) {
			return ItemStack.EMPTY;
		}
		int slot = slots.get(this.random.nextInt(slots.size()));
		ItemStack taken = items.get(slot).split(1);
		this.stolen.addItem(taken.copy());
		player.getInventory().setChanged();
		this.setPersistenceRequired(); // never despawn with your things
		this.cooldown = SNATCH_COOLDOWN;
		level.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 1.0, player.getZ(), 12, 0.4, 0.4, 0.4, 0.05);
		level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.3, this.getZ(), 10, 0.5, 0.2, 0.5, 0.02);
		level.playSound(null, this.getX(), this.getY(), this.getZ(), ModSounds.SUPER_PHANTOM_SNATCH, SoundSource.HOSTILE, 1.0F, 1.0F);
		return taken;
	}

	public int getCooldown() {
		return this.cooldown;
	}

	/** For tests and screenshots. */
	public void setCooldownForTest(final int ticks) {
		this.cooldown = ticks;
	}

	public List<ItemStack> getStolenItems() {
		List<ItemStack> out = new ArrayList<>();
		for (int i = 0; i < this.stolen.getContainerSize(); i++) {
			if (!this.stolen.getItem(i).isEmpty()) {
				out.add(this.stolen.getItem(i).copy());
			}
		}
		return out;
	}

	@Override
	protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource source, final boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		for (ItemStack stack : this.stolen.removeAllItems()) {
			this.spawnAtLocation(level, stack);
		}
	}

	@Override
	protected void addAdditionalSaveData(final ValueOutput output) {
		super.addAdditionalSaveData(output);
		this.stolen.storeAsItemList(output.list("Stolen", ItemStack.CODEC));
		output.putInt("SnatchCooldown", this.cooldown);
	}

	@Override
	protected void readAdditionalSaveData(final ValueInput input) {
		super.readAdditionalSaveData(input);
		input.list("Stolen", ItemStack.CODEC).ifPresent(this.stolen::fromItemList);
		this.cooldown = input.getIntOr("SnatchCooldown", 0);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.PHANTOM_AMBIENT;
	}

	@Override
	public float getVoicePitch() {
		return 0.7F; // deeper than a normal phantom
	}

	@Override
	protected SoundEvent getHurtSound(final DamageSource source) {
		return SoundEvents.PHANTOM_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PHANTOM_DEATH;
	}

	@Override
	public boolean doHurtTarget(final ServerLevel level, final Entity target) {
		return false; // never deals damage
	}

	@Override
	public boolean causeFallDamage(final double fallDistance, final float damageModifier, final DamageSource damageSource) {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(final double distSqr) {
		return !this.hasCustomName() && !this.isPersistenceRequired();
	}

	/** Slowly swoops toward a nearby player when ready to snatch. */
	class SwoopGoal extends Goal {
		private @Nullable Player target;

		SwoopGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (SuperPhantom.this.cooldown > 0 || SuperPhantom.this.random.nextInt(20) != 0) {
				return false;
			}
			this.target = SuperPhantom.this.findSnatchable(16.0);
			return this.target != null;
		}

		@Override
		public boolean canContinueToUse() {
			return this.target != null && canBeSnatchedFrom(this.target) && SuperPhantom.this.cooldown == 0
					&& this.target.distanceToSqr(SuperPhantom.this) < 400;
		}

		@Override
		public void tick() {
			if (this.target != null) {
				SuperPhantom.this.getNavigation().moveTo(this.target.getX(), this.target.getY() + 1.0, this.target.getZ(), 1.0);
			}
		}

		@Override
		public void stop() {
			this.target = null;
		}
	}
}

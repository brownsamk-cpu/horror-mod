package com.horrormod;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Mysterious Witch (horrormod:mysterious_witch): a NEUTRAL witch with purple and teal robes, a crooked starry hat and
 * glowing green eyes. She wanders peacefully and ignores players until one hits her. Then, for about 30 seconds, she
 * throws her own mod splash potions (Bouncy, Glow, Bat, Slime Feet) at that attacker, and calms down afterward.
 * She sometimes drops one of her potions when defeated. Rare, at night in dark forests and swamps.
 */
public class MysteriousWitch extends Monster implements RangedAttackMob {
	public static final int ANGER_TICKS = 600; // 30 s
	public static final float POTION_DROP_CHANCE = 0.4F;
	private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(MysteriousWitch.class, EntityDataSerializers.BOOLEAN);
	private int angerTime;
	private int potionsThrown;

	public MysteriousWitch(final EntityType<? extends MysteriousWitch> type, final Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 26.0)
				.add(Attributes.MOVEMENT_SPEED, 0.25);
	}

	public static boolean checkWitchSpawnRules(final EntityType<MysteriousWitch> type, final ServerLevelAccessor level, final EntitySpawnReason reason,
			final BlockPos pos, final RandomSource random) {
		return Monster.checkMonsterSpawnRules(type, level, reason, pos, random)
				&& (EntitySpawnReason.isSpawner(reason) || level.getLevel().isDarkOutside() || level.getRawBrightness(pos, 0) < 6);
	}

	@Override
	protected void defineSynchedData(final SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(ANGRY, false);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 50, 10.0F));
		this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		// NEUTRAL: the only target goal is "whoever hurt me"
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	@Override
	public boolean hurtServer(final ServerLevel level, final DamageSource source, final float damage) {
		boolean hurt = super.hurtServer(level, source, damage);
		if (hurt && source.getEntity() instanceof LivingEntity attacker && attacker != this) {
			this.angerTime = ANGER_TICKS;
			this.setTarget(attacker);
		}
		return hurt;
	}

	@Override
	protected void customServerAiStep(final ServerLevel level) {
		super.customServerAiStep(level);
		if (this.angerTime > 0) {
			this.angerTime--;
			if (this.angerTime == 0) {
				this.setTarget(null); // calms down
				this.setLastHurtByMob(null);
			}
		} else if (this.getTarget() != null) {
			this.setTarget(null);
		}
		this.entityData.set(ANGRY, this.getTarget() != null);
	}

	public boolean isAngry() {
		return this.entityData.get(ANGRY);
	}

	public int getAngerTime() {
		return this.angerTime;
	}

	/** For tests. */
	public void setAngerTimeForTest(final int ticks) {
		this.angerTime = ticks;
	}

	public int getPotionsThrown() {
		return this.potionsThrown;
	}

	public static ItemStack randomPotion(final RandomSource random, final net.minecraft.world.item.Item item) {
		List<Holder<Potion>> all = MysteriousPotions.potions();
		return PotionContents.createItemStack(item, all.get(random.nextInt(all.size())));
	}

	@Override
	public void performRangedAttack(final LivingEntity target, final float power) {
		Vec3 tm = target.getDeltaMovement();
		double xd = target.getX() + tm.x - this.getX();
		double yd = target.getEyeY() - 1.1F - this.getY();
		double zd = target.getZ() + tm.z - this.getZ();
		double dist = Math.sqrt(xd * xd + zd * zd);
		if (this.level() instanceof ServerLevel level) {
			Projectile.spawnProjectileUsingShoot(ThrownSplashPotion::new, level, randomPotion(this.random, Items.SPLASH_POTION), this, xd, yd + dist * 0.2, zd,
					dist <= 2.0 ? 0.45F : 0.75F, this.rangedAttackUncertainty(level));
			this.potionsThrown++;
		}
		if (!this.isSilent()) {
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITCH_THROW, this.getSoundSource(), 1.0F,
					0.8F + this.random.nextFloat() * 0.4F);
		}
	}

	@Override
	protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource source, final boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		if (this.random.nextFloat() < POTION_DROP_CHANCE) {
			this.dropPotion(level);
		}
	}

	/** Drops one of her drinkable potions. */
	public void dropPotion(final ServerLevel level) {
		this.spawnAtLocation(level, randomPotion(this.random, Items.POTION));
	}

	@Override
	protected void addAdditionalSaveData(final ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("AngerTime", this.angerTime);
	}

	@Override
	protected void readAdditionalSaveData(final ValueInput input) {
		super.readAdditionalSaveData(input);
		this.angerTime = input.getIntOr("AngerTime", 0);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return ModSounds.MYSTERIOUS_WITCH_CACKLE;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 240;
	}

	@Override
	protected SoundEvent getHurtSound(final DamageSource source) {
		return SoundEvents.WITCH_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WITCH_DEATH;
	}

	@Override
	public boolean doHurtTarget(final ServerLevel level, final Entity target) {
		return false; // she only throws potions, no melee
	}
}

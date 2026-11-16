package com.horrormod.cave;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Stone Monster (horrormod:stone_monster), June and Leon's mini boss: a 5-blocks-tall rocky giant with glowing red
 * eyes that guards the Stony Cave. It winds up and THROWS rocks at you (not bullets!), and if you get too close it lifts
 * both fists and SLAMS the ground. A boss health bar shows while you're nearby. 150 health so a kid can beat it, and it
 * always drops the Stone Key that opens the Stone Door. Spooky-fun, never gory.
 */
public class StoneMonster extends Monster implements RangedAttackMob {
	public static final float MAX_HEALTH = 150.0F;
	public static final float ROCK_DAMAGE = 4.0F; // 2 hearts
	public static final float SLAM_DAMAGE = 5.0F;
	public static final double SLAM_RANGE = 4.0;
	public static final double BOSS_BAR_RANGE = 24.0;
	public static final int THROW_TICKS = 16, THROW_RELEASE = 8;
	public static final int SLAM_TICKS = 20, SLAM_HIT = 10, SLAM_COOLDOWN = 60;
	public static final byte EVENT_THROW = 4, EVENT_SLAM = 11;

	private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(java.util.UUID.randomUUID(),
			Component.translatable("entity.horrormod.stone_monster"), BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_10).setDarkenScreen(false);
	private int throwTicks, slamTicks, slamCooldown, rocksThrown, slams;
	private @Nullable LivingEntity throwTarget;
	/** Client-side animation timers (counted down every tick on the client). */
	private int clientThrowTicks, clientSlamTicks;

	public StoneMonster(final EntityType<? extends StoneMonster> type, final Level level) {
		super(type, level);
		this.xpReward = 30;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, MAX_HEALTH)
				.add(Attributes.MOVEMENT_SPEED, 0.22)
				.add(Attributes.ARMOR, 4.0)
				.add(Attributes.ATTACK_DAMAGE, SLAM_DAMAGE)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
				.add(Attributes.FOLLOW_RANGE, 24.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 40, 16.0F));
		this.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 1.0));
		this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 16.0F));
		this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	// ---- throwing rocks ----

	@Override
	public void performRangedAttack(final LivingEntity target, final float power) {
		if (this.throwTicks > 0 || this.slamTicks > 0) {
			return;
		}
		this.throwTicks = THROW_TICKS;
		this.throwTarget = target;
		this.level().broadcastEntityEvent(this, EVENT_THROW);
		this.playSound(SoundEvents.GRINDSTONE_USE, 1.0F, 0.5F);
	}

	/** Throws one rock at the target right now (the release point of the throw animation). */
	public @Nullable StoneRock throwRockAt(final LivingEntity target) {
		if (!(this.level() instanceof ServerLevel level)) {
			return null;
		}
		Vec3 hand = this.position().add(0, this.getBbHeight() * 0.8, 0);
		double xd = target.getX() - hand.x;
		double yd = target.getY(0.5) - hand.y;
		double zd = target.getZ() - hand.z;
		double dist = Math.sqrt(xd * xd + zd * zd);
		StoneRock rock = new StoneRock(level, this, new ItemStack(CaveItems.STONE_ROCK));
		rock.setPos(hand.x, hand.y, hand.z);
		Projectile.spawnProjectileUsingShoot(rock, level, new ItemStack(CaveItems.STONE_ROCK), xd, yd + dist * 0.12, zd, 1.3F, 4.0F);
		this.rocksThrown++;
		level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 1.0F, 0.4F);
		return rock;
	}

	// ---- ground slam ----

	/** Starts the both-fists ground slam (it lands {@link #SLAM_TICKS} - {@link #SLAM_HIT} ticks later). */
	public void startSlam() {
		this.slamTicks = SLAM_TICKS;
		this.slamCooldown = SLAM_COOLDOWN;
		this.level().broadcastEntityEvent(this, EVENT_SLAM);
		this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 0.5F);
	}

	private void slamLands(final ServerLevel level) {
		this.slams++;
		level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 0.6F, 1.4F);
		level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), this.getX(), this.getY() + 0.2, this.getZ(),
				60, 2.0, 0.2, 2.0, 0.15);
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(SLAM_RANGE, 1.0, SLAM_RANGE), e -> e != this && e.isAlive())) {
			if (e instanceof StoneMonster) {
				continue;
			}
			if (e.hurtServer(level, this.damageSources().mobAttack(this), SLAM_DAMAGE)) {
				Vec3 away = e.position().subtract(this.position()).multiply(1, 0, 1);
				away = away.lengthSqr() < 1.0E-4 ? new Vec3(0, 0, 1) : away.normalize();
				e.push(away.x * 0.6, 0.4, away.z * 0.6);
				e.needsSync = true;
				if (e instanceof ServerPlayer sp) {
					sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(sp));
				}
			}
		}
	}

	@Override
	protected void customServerAiStep(final ServerLevel level) {
		super.customServerAiStep(level);
		if (this.slamCooldown > 0) {
			this.slamCooldown--;
		}
		if (this.throwTicks > 0) {
			this.throwTicks--;
			if (this.throwTicks == THROW_RELEASE && this.throwTarget != null && this.throwTarget.isAlive()) {
				this.throwRockAt(this.throwTarget);
			}
			if (this.throwTicks == 0) {
				this.throwTarget = null;
			}
		}
		if (this.slamTicks > 0) {
			this.slamTicks--;
			if (this.slamTicks == SLAM_HIT) {
				this.slamLands(level);
			}
		} else {
			LivingEntity target = this.getTarget();
			if (target != null && target.isAlive() && this.slamCooldown <= 0 && this.throwTicks == 0
					&& this.distanceToSqr(target) < (SLAM_RANGE + 1.0) * (SLAM_RANGE + 1.0)) {
				this.startSlam();
			}
		}
		this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
		if (this.tickCount % 10 == 0) {
			this.updateBossBarPlayers(level);
		}
		// creepy cave sounds while someone is near
		if (this.tickCount % 160 == 0 && level.getNearestPlayer(this, 16.0) != null) {
			level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 0.7F,
					0.8F + this.random.nextFloat() * 0.2F);
		}
	}

	/** Shows the boss bar to players within {@link #BOSS_BAR_RANGE} blocks and hides it from everyone else. */
	public void updateBossBarPlayers(final ServerLevel level) {
		Set<ServerPlayer> near = new HashSet<>();
		for (ServerPlayer p : level.players()) {
			if (p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) <= BOSS_BAR_RANGE * BOSS_BAR_RANGE) {
				near.add(p);
			}
		}
		for (ServerPlayer p : Set.copyOf(this.bossEvent.getPlayers())) {
			if (!near.contains(p)) {
				this.bossEvent.removePlayer(p);
			}
		}
		for (ServerPlayer p : near) {
			if (!this.bossEvent.getPlayers().contains(p)) {
				this.bossEvent.addPlayer(p);
			}
		}
	}

	public ServerBossEvent getBossEvent() {
		return this.bossEvent;
	}

	@Override
	public void stopSeenByPlayer(final ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.bossEvent.removePlayer(player);
	}

	@Override
	public void remove(final Entity.RemovalReason reason) {
		super.remove(reason);
		this.bossEvent.removeAllPlayers();
	}

	@Override
	public void setCustomName(final @Nullable Component name) {
		super.setCustomName(name);
		this.bossEvent.setName(this.getDisplayName());
	}

	// ---- animations (client) ----

	@Override
	public void handleEntityEvent(final byte id) {
		if (id == EVENT_THROW) {
			this.clientThrowTicks = THROW_TICKS;
		} else if (id == EVENT_SLAM) {
			this.clientSlamTicks = SLAM_TICKS;
		} else {
			super.handleEntityEvent(id);
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide()) {
			if (this.clientThrowTicks > 0) {
				this.clientThrowTicks--;
			}
			if (this.clientSlamTicks > 0) {
				this.clientSlamTicks--;
			}
		}
	}

	/** 0 = not throwing, goes 0 to 1 through the wind-up and throw. */
	public float getThrowProgress(final float partialTicks) {
		return this.clientThrowTicks <= 0 ? 0.0F : Math.min(1.0F, (THROW_TICKS - this.clientThrowTicks + partialTicks) / THROW_TICKS);
	}

	/** 0 = not slamming, goes 0 to 1 through lifting the fists and slamming them down. */
	public float getSlamProgress(final float partialTicks) {
		return this.clientSlamTicks <= 0 ? 0.0F : Math.min(1.0F, (SLAM_TICKS - this.clientSlamTicks + partialTicks) / SLAM_TICKS);
	}

	/** For tests: stop picking targets by itself (so it can't wander off to bother other tests). */
	public void stopTargetingForTest() {
		this.targetSelector.removeAllGoals(g -> true);
		this.setTarget(null);
	}

	public int getRocksThrown() {
		return this.rocksThrown;
	}

	public int getSlams() {
		return this.slams;
	}

	// ---- loot, sounds, misc ----

	@Override
	protected void dropCustomDeathLoot(final ServerLevel level, final DamageSource source, final boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		this.spawnAtLocation(level, new ItemStack(CaveItems.STONE_KEY));
	}

	@Override
	public boolean removeWhenFarAway(final double distSqr) {
		return false; // a guardian stays put
	}

	@Override
	public boolean causeFallDamage(final double fallDistance, final float multiplier, final DamageSource source) {
		return false;
	}

	@Override
	public boolean doHurtTarget(final ServerLevel level, final Entity target) {
		return false; // it only throws rocks and slams
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return SoundEvents.GRINDSTONE_USE;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 200;
	}

	@Override
	public float getVoicePitch() {
		return 0.4F;
	}

	@Override
	protected SoundEvent getHurtSound(final DamageSource source) {
		return SoundEvents.STONE_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.IRON_GOLEM_DEATH;
	}

	@Override
	protected void addAdditionalSaveData(final ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt("SlamCooldown", this.slamCooldown);
	}

	@Override
	protected void readAdditionalSaveData(final ValueInput input) {
		super.readAdditionalSaveData(input);
		this.slamCooldown = input.getIntOr("SlamCooldown", 0);
		if (this.hasCustomName()) {
			this.bossEvent.setName(this.getDisplayName());
		}
	}
}

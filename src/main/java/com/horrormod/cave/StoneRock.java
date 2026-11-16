package com.horrormod.cave;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** A rock thrown by the Stone Monster: 2 hearts of damage, then it crumbles into little stone bits. */
public class StoneRock extends ThrowableItemProjectile {
	public StoneRock(final EntityType<? extends StoneRock> type, final Level level) {
		super(type, level);
	}

	public StoneRock(final Level level, final LivingEntity owner, final ItemStack stack) {
		super(CaveEntities.STONE_ROCK, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return CaveItems.STONE_ROCK;
	}

	@Override
	public void handleEntityEvent(final byte id) {
		if (id == 3) {
			ItemParticleOption bits = new ItemParticleOption(ParticleTypes.ITEM, CaveItems.STONE_ROCK);
			for (int i = 0; i < 8; i++) {
				this.level().addParticle(bits, this.getX(), this.getY(), this.getZ(), (this.random.nextDouble() - 0.5) * 0.1, 0.1, (this.random.nextDouble() - 0.5) * 0.1);
			}
		} else {
			super.handleEntityEvent(id);
		}
	}

	@Override
	protected void onHitEntity(final EntityHitResult hit) {
		super.onHitEntity(hit);
		Entity target = hit.getEntity();
		if (this.level() instanceof ServerLevel level && !(target instanceof StoneMonster)) {
			target.hurtServer(level, this.damageSources().thrown(this, this.getOwner()), StoneMonster.ROCK_DAMAGE);
		}
	}

	@Override
	protected void onHit(final HitResult result) {
		super.onHit(result);
		if (this.level() instanceof ServerLevel level) {
			level.broadcastEntityEvent(this, (byte) 3);
			level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 0.8F, 0.8F);
			this.discard();
		}
	}
}

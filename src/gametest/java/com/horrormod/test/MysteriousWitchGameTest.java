package com.horrormod.test;

import com.horrormod.ModEntities;
import com.horrormod.ModItems;
import com.horrormod.ModSounds;
import com.horrormod.MysteriousPotions;
import com.horrormod.MysteriousWitch;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.Vec3;

/** Horror Mod 0.4.0, step 3: the Mysterious Witch (horrormod:mysterious_witch) and her 4 new potions. */
public class MysteriousWitchGameTest {
	/** Spawns, is named "Mysterious Witch", has a spawn egg and her own cackle; all 4 potions are registered. */
	@GameTest
	public void witchSpawns(GameTestHelper helper) {
		MysteriousWitch w = helper.spawn(ModEntities.MYSTERIOUS_WITCH, new BlockPos(1, 2, 1));
		helper.assertTrue(w.isAlive(), "should spawn");
		helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.getKey(ModEntities.MYSTERIOUS_WITCH).toString().equals("horrormod:mysterious_witch"), "id");
		helper.assertTrue(w.getType().getDescriptionId().equals("entity.horrormod.mysterious_witch"), "lang key");
		helper.assertTrue(ModItems.MYSTERIOUS_WITCH_SPAWN_EGG instanceof SpawnEggItem, "spawn egg");
		helper.assertTrue(ModSounds.MYSTERIOUS_WITCH_CACKLE != null, "cackle");
		helper.assertTrue(MysteriousPotions.ALL.size() == 4, "4 potions");
		for (var e : MysteriousPotions.ALL.values()) {
			helper.assertTrue(BuiltInRegistries.POTION.getKey(e.potion().value()).getNamespace().equals("horrormod"), "mod potion " + e.id());
			helper.assertTrue(BuiltInRegistries.MOB_EFFECT.getKey(e.effect().value()).getNamespace().equals("horrormod"), "mod effect " + e.id());
		}
		helper.succeed();
	}

	/** Neutral: a zombie-free test area, a player... she ignores everyone and throws nothing until hit. */
	@GameTest(maxTicks = 120)
	@SuppressWarnings("removal")
	public void witchIsNeutralUntilHit(GameTestHelper helper) {
		MysteriousWitch w = helper.spawn(ModEntities.MYSTERIOUS_WITCH, new BlockPos(1, 2, 1));
		Pig pig = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 4));
		helper.runAfterDelay(100, () -> {
			helper.assertTrue(w.getTarget() == null && !w.isAngry(), "should ignore everyone");
			helper.assertTrue(w.getPotionsThrown() == 0, "threw potions without being hit");
			helper.assertTrue(pig.isAlive(), "pig fine");
			helper.succeed();
		});
	}

	/** Hit her and she gets angry at the attacker and throws her own splash potions at it. */
	@GameTest(maxTicks = 200)
	public void witchThrowsPotionsWhenHit(GameTestHelper helper) {
		MysteriousWitch w = helper.spawn(ModEntities.MYSTERIOUS_WITCH, new BlockPos(1, 2, 1));
		Zombie z = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(1, 2, 6));
		z.setNoAi(true);
		w.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(z), 1.0F);
		helper.assertTrue(w.getTarget() == z && w.getAngerTime() == MysteriousWitch.ANGER_TICKS, "angry at the attacker");
		helper.succeedWhen(() -> {
			helper.assertTrue(w.getPotionsThrown() >= 1, "no potion thrown yet");
			var potions = helper.getLevel().getEntitiesOfClass(ThrownSplashPotion.class, helper.getBounds().inflate(4));
			boolean modPotion = potions.isEmpty() || potions.stream().allMatch(p -> p.getItem().getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
					PotionContents.EMPTY).potion().map(h -> MysteriousPotions.potions().contains(h)).orElse(false));
			helper.assertTrue(modPotion, "she throws only her own mod potions");
		});
	}

	/** She calms down after her anger runs out. */
	@GameTest(maxTicks = 40)
	public void witchCalmsDown(GameTestHelper helper) {
		MysteriousWitch w = helper.spawn(ModEntities.MYSTERIOUS_WITCH, new BlockPos(1, 2, 1));
		Zombie z = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(1, 2, 6));
		z.setNoAi(true);
		w.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(z), 1.0F);
		w.setAngerTimeForTest(2);
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(w.getTarget() == null, "should calm down");
			helper.succeed();
		});
	}

	/** Bouncy: a mob standing on the ground gets bounced up. */
	@GameTest(maxTicks = 60)
	public void bouncyPotionBounces(GameTestHelper helper) {
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(1, 2, 1));
		double[] ground = new double[1];
		helper.runAfterDelay(10, () -> {
			ground[0] = pig.getY(); // settled on the floor
			pig.addEffect(new MobEffectInstance(MysteriousPotions.BOUNCY, 200));
		});
		helper.runAfterDelay(12, () -> helper.succeedWhen(() ->
				helper.assertTrue(pig.getY() > ground[0] + 0.4, "should bounce up, dy=" + (pig.getY() - ground[0]))));
	}

	/** Glow: you glow. */
	@GameTest(maxTicks = 20)
	public void glowPotionGlows(GameTestHelper helper) {
		Pig pig = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 1));
		pig.addEffect(new MobEffectInstance(MysteriousPotions.GLOW, 200));
		helper.succeedWhen(() -> helper.assertTrue(pig.hasEffect(MobEffects.GLOWING) && pig.isCurrentlyGlowing(), "should glow"));
	}

	/** Bat: the effect applies and ticks (bats are particles, the tint is client-side). */
	@GameTest(maxTicks = 20)
	public void batPotionApplies(GameTestHelper helper) {
		Pig pig = helper.spawn(EntityTypes.PIG, new BlockPos(1, 2, 1));
		pig.addEffect(new MobEffectInstance(MysteriousPotions.BAT, 200));
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(pig.hasEffect(MysteriousPotions.BAT) && pig.getHealth() == pig.getMaxHealth(), "bat effect, harmless");
			helper.succeed();
		});
	}

	/** Slime Feet: a pushed mob slides much farther than a normal one. */
	@GameTest(maxTicks = 60)
	public void slimeFeetPotionSlides(GameTestHelper helper) {
		Pig slimy = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(1, 2, 1));
		Pig normal = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 2, 1));
		slimy.addEffect(new MobEffectInstance(MysteriousPotions.SLIME_FEET, 200));
		double s0 = slimy.getZ(), n0 = normal.getZ();
		helper.runAfterDelay(5, () -> {
			slimy.setDeltaMovement(new Vec3(0, 0, 0.5));
			normal.setDeltaMovement(new Vec3(0, 0, 0.5));
		});
		helper.runAfterDelay(30, () -> {
			double ds = slimy.getZ() - s0, dn = normal.getZ() - n0;
			helper.assertTrue(ds > dn * 1.5, "slime feet should slide farther: slimy=" + ds + " normal=" + dn);
			helper.succeed();
		});
	}

	/** She drops one of her drinkable potions. */
	@GameTest(maxTicks = 20)
	public void witchDropsPotion(GameTestHelper helper) {
		MysteriousWitch w = helper.spawn(ModEntities.MYSTERIOUS_WITCH, new BlockPos(1, 2, 1));
		w.dropPotion(helper.getLevel());
		helper.runAfterDelay(2, () -> {
			var items = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(2));
			helper.assertTrue(items.stream().anyMatch(i -> i.getItem().is(Items.POTION)
					&& i.getItem().get(net.minecraft.core.component.DataComponents.POTION_CONTENTS).potion().map(h -> MysteriousPotions.potions().contains(h)).orElse(false)),
					"should drop a Mysterious potion");
			helper.assertTrue(MysteriousWitch.POTION_DROP_CHANCE > 0 && MysteriousWitch.POTION_DROP_CHANCE < 1, "sometimes");
			helper.succeed();
		});
	}
}

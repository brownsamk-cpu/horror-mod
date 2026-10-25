package com.horrormod.test;

import com.horrormod.ModEntities;
import com.horrormod.ModItems;
import com.horrormod.ModSounds;
import com.horrormod.SuperPhantom;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;

/** Horror Mod 0.3.0, step 2 (now hostile): the Super Phantom (horrormod:super_phantom). */
public class SuperPhantomGameTest {
	private static SuperPhantom spawn(GameTestHelper helper) {
		SuperPhantom p = helper.spawn(ModEntities.SUPER_PHANTOM, new BlockPos(1, 3, 1));
		p.setNoAi(true);
		return p;
	}

	/** It spawns, is named "Super Phantom", has a spawn egg, and flies (no gravity). The Ghost is unchanged. */
	@GameTest
	public void superPhantomSpawns(GameTestHelper helper) {
		SuperPhantom p = spawn(helper);
		helper.assertTrue(p.isAlive() && p.getType() == ModEntities.SUPER_PHANTOM, "should spawn");
		helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.getKey(ModEntities.SUPER_PHANTOM).toString().equals("horrormod:super_phantom"), "id");
		helper.assertTrue(p.getType().getDescriptionId().equals("entity.horrormod.super_phantom"), "lang key");
		helper.assertTrue(ModItems.SUPER_PHANTOM_SPAWN_EGG instanceof SpawnEggItem, "spawn egg");
		helper.assertTrue(p.isNoGravity(), "flies");
		helper.assertTrue(ModSounds.SUPER_PHANTOM_SNATCH != null, "snatch sound");
		helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.getKey(ModEntities.SPOOKY_SPIRIT).toString().equals("horrormod:spooky_spirit"), "ghost id kept");
		helper.succeed();
	}

	/** Hostile (0.3.0): a monster that bites a survival player for damage, and targets survival players at night. */
	@GameTest(maxTicks = 100)
	@SuppressWarnings("removal")
	public void superPhantomAttacks(GameTestHelper helper) {
		helper.getLevel().getServer().setDifficulty(net.minecraft.world.Difficulty.NORMAL, true);
		SuperPhantom p = spawn(helper);
		helper.assertTrue(ModEntities.SUPER_PHANTOM.getCategory() == MobCategory.MONSTER, "hostile monster category");
		helper.assertTrue(p.getAttributeValue(Attributes.ATTACK_DAMAGE) == 2.0, "about 2 hp per bite");
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		Vec3 pos = helper.absoluteVec(new Vec3(1.5, 2, 2.5));
		player.snapTo(pos.x, pos.y, pos.z, 0, 0);
		float health = player.getHealth();
		helper.assertTrue(p.doHurtTarget(helper.getLevel(), player), "bite should land");
		helper.assertTrue(player.getHealth() < health, "player should take damage");
		helper.assertTrue(health - player.getHealth() <= 3.0F, "phantom-level damage, took " + (health - player.getHealth()));
		helper.succeed();
	}

	/** Snatches exactly ONE item from the main inventory, never armor, and stores it. */
	@GameTest
	public void superPhantomStealsOneItem(GameTestHelper helper) {
		SuperPhantom p = spawn(helper);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 5));
		player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
		ItemStack taken = p.trySnatch(helper.getLevel(), player);
		helper.assertTrue(taken.is(Items.DIAMOND) && taken.getCount() == 1, "should take 1 diamond, took " + taken);
		helper.assertTrue(player.getInventory().getItem(0).getCount() == 4, "4 diamonds left");
		helper.assertTrue(player.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET), "armor is never taken");
		helper.assertTrue(p.getStolenItems().size() == 1 && p.getStolenItems().get(0).is(Items.DIAMOND), "phantom keeps it");
		helper.assertTrue(p.isPersistenceRequired(), "won't despawn with your stuff");
		// creative players are left alone
		SuperPhantom p2 = spawn(helper);
		Player creative = helper.makeMockPlayer(GameType.CREATIVE);
		creative.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 1));
		helper.assertTrue(p2.trySnatch(helper.getLevel(), creative).isEmpty(), "never snatches from creative");
		helper.succeed();
	}

	/** About 30 s cooldown: a second snatch right away does nothing; after the cooldown it can snatch again. */
	@GameTest(maxTicks = 700)
	public void superPhantomCooldown(GameTestHelper helper) {
		SuperPhantom p = helper.spawn(ModEntities.SUPER_PHANTOM, new BlockPos(1, 3, 1));
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.getInventory().setItem(0, new ItemStack(Items.APPLE, 10));
		helper.assertFalse(p.trySnatch(helper.getLevel(), player).isEmpty(), "first snatch");
		helper.assertTrue(p.getCooldown() == SuperPhantom.SNATCH_COOLDOWN && SuperPhantom.SNATCH_COOLDOWN >= 500, "~30 s cooldown");
		helper.assertTrue(p.trySnatch(helper.getLevel(), player).isEmpty(), "no snatch during cooldown");
		helper.assertTrue(player.getInventory().getItem(0).getCount() == 9, "only one taken");
		helper.succeedWhen(() -> {
			helper.assertTrue(p.getCooldown() == 0, "cooldown still running");
			helper.assertFalse(p.trySnatch(helper.getLevel(), player).isEmpty(), "can snatch again after cooldown");
		});
	}

	/** Drops ALL stolen items when it dies, so you get them back. */
	@GameTest(maxTicks = 60)
	public void superPhantomDropsStolenItems(GameTestHelper helper) {
		SuperPhantom p = spawn(helper);
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 1));
		p.trySnatch(helper.getLevel(), player);
		player.getInventory().setItem(0, new ItemStack(Items.EMERALD, 1));
		p.setCooldownForTest(0);
		p.trySnatch(helper.getLevel(), player);
		helper.assertTrue(p.getStolenItems().size() == 2, "stole 2 things");
		helper.assertTrue(player.getInventory().getItem(0).isEmpty(), "emerald taken");
		p.kill(helper.getLevel());
		helper.runAfterDelay(5, () -> {
			helper.assertItemEntityPresent(Items.DIAMOND);
			helper.assertItemEntityPresent(Items.EMERALD);
			helper.succeed();
		});
	}
}

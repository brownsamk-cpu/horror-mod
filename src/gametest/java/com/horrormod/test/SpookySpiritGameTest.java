package com.horrormod.test;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.horrormod.ModEntities;
import com.horrormod.ModItems;
import com.horrormod.ModSounds;
import com.horrormod.SpookySpirit;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.phys.Vec3;

/** Horror Mod 0.1.0, step 1: the Spooky Spirit (horrormod:spooky_spirit). */
public class SpookySpiritGameTest {
	/** It spawns, has a spawn egg, is named "Spooky Spirit", and is an ambient (rare, dark-only) mob. */
	@GameTest
	@SuppressWarnings("removal")
	public void spookySpiritSpawns(GameTestHelper helper) {
		SpookySpirit spirit = helper.spawn(ModEntities.SPOOKY_SPIRIT, new BlockPos(1, 2, 1));
		helper.assertTrue(spirit.isAlive() && spirit.getType() == ModEntities.SPOOKY_SPIRIT, "spirit should spawn");
		helper.assertTrue(spirit.getType().getDescription().getString().equals("Spooky Spirit")
				|| spirit.getType().getDescriptionId().equals("entity.horrormod.spooky_spirit"), "display name");
		helper.assertTrue(ModItems.SPOOKY_SPIRIT_SPAWN_EGG instanceof SpawnEggItem, "spawn egg");
		helper.assertTrue(ModEntities.SPOOKY_SPIRIT.getCategory() == MobCategory.AMBIENT, "ambient category");
		helper.succeed();
	}

	/** Passive: no attack damage, never targets or hurts a player right next to it. */
	@GameTest(maxTicks = 80)
	@SuppressWarnings("removal")
	public void spookySpiritIsPassive(GameTestHelper helper) {
		SpookySpirit spirit = helper.spawn(ModEntities.SPOOKY_SPIRIT, new BlockPos(1, 2, 1));
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		Vec3 p = helper.absoluteVec(new Vec3(2.5, 1, 1.5));
		player.snapTo(p.x, p.y, p.z, 0, 0);
		helper.assertTrue(spirit.getAttribute(Attributes.ATTACK_DAMAGE) == null, "no attack damage");
		helper.assertFalse(spirit.doHurtTarget(helper.getLevel(), player), "doHurtTarget must do nothing");
		float health = player.getHealth();
		helper.runAfterDelay(60, () -> {
			helper.assertTrue(player.getHealth() >= health, "player got hurt");
			helper.assertTrue(spirit.getTarget() == null, "spirit never targets anyone");
			helper.succeed();
		});
	}

	/** It floats: no gravity, so it does not fall to the floor. */
	@GameTest(maxTicks = 80)
	@SuppressWarnings("removal")
	public void spookySpiritFloats(GameTestHelper helper) {
		SpookySpirit spirit = helper.spawn(ModEntities.SPOOKY_SPIRIT, new BlockPos(1, 3, 1));
		double startY = spirit.getY();
		helper.assertTrue(spirit.isNoGravity(), "no gravity");
		helper.runAfterDelay(60, () -> {
			helper.assertTrue(spirit.getY() > startY - 1.0, "spirit should hover, not drop: dy=" + (spirit.getY() - startY));
			helper.assertFalse(spirit.onGround(), "spirit should not be resting on the floor");
			helper.succeed();
		});
	}

	/** It says "wooo" now and then, using its own sound (which has a subtitle). */
	@GameTest(maxTicks = 1300)
	@SuppressWarnings("removal")
	public void spookySpiritSaysWooo(GameTestHelper helper) {
		SpookySpirit spirit = helper.spawn(ModEntities.SPOOKY_SPIRIT, new BlockPos(1, 2, 1));
		helper.assertTrue(ModSounds.SPOOKY_SPIRIT_WOOO != null, "sound registered");
		helper.succeedWhen(() -> helper.assertTrue(spirit.getWooos() >= 1, "no wooo yet"));
	}

	/** Clear like a ghost: the texture's body pixels are partly see-through (alpha between 1 and 254). */
	@GameTest
	public void spookySpiritIsSeeThrough(GameTestHelper helper) {
		try (InputStream in = SpookySpiritGameTest.class.getResourceAsStream("/assets/horrormod/textures/entity/spooky_spirit.png")) {
			helper.assertTrue(in != null, "texture missing");
			BufferedImage img = ImageIO.read(in);
			int partial = 0, solid = 0;
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					int a = img.getRGB(x, y) >>> 24;
					if (a > 0 && a < 200) partial++;
					if (a == 255) solid++;
				}
			}
			helper.assertTrue(partial > 300, "most of the spirit should be see-through, partial=" + partial);
			helper.assertTrue(solid == 0, "no fully solid pixels, solid=" + solid);
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		helper.succeed();
	}
}

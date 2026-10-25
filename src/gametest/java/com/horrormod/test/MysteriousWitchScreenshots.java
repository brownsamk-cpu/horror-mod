package com.horrormod.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.horrormod.ModEntities;
import com.horrormod.MysteriousWitch;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;

/**
 * Dev-only: the Mysterious Witch close-up, her throwing a potion after being hit, and her 4 potions in the inventory.
 * Run with: DISPLAY=:77 LIBGL_ALWAYS_SOFTWARE=1 GALLIUM_DRIVER=llvmpipe SDL_VIDEO_FORCE_EGL=1 ALSOFT_DRIVERS=null ./gradlew runClientGameTest -PclientShots
 */
public class MysteriousWitchScreenshots implements FabricClientGameTest {
	private static final List<String> SETUP = List.of("gamerule send_command_feedback false", "gamemode creative @a", "weather clear",
			"gamerule advance_time false", "gamerule advance_weather false", "gamerule spawn_mobs false", "gamerule announce_advancements false",
			"advancement grant @a everything", "recipe give @a *");
	private static final List<String> IDS = List.of("bouncy", "glow", "bat", "slime_feet");

	@Override
	public void runTest(ClientGameTestContext context) {
		Path out = Path.of(System.getProperty("horrormod.shots", "build/mysterious_witch_shots"));
		context.getInput().resizeWindow(1280, 720);
		context.runOnClient(c -> c.options.showSubtitles().set(true));
		context.runOnClient(c -> c.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			var server = sp.getServer();
			sp.getConnection().waitForChunksRender();
			SETUP.forEach(server::runCommand);
			server.runCommand("time set 13500");
			server.runCommand("tp @a 0.5 100 -20");
			server.runCommand("fill -16 99 -10 16 99 20 minecraft:grass_block");
			server.runCommand("fill -16 100 -10 16 120 20 minecraft:air");
			server.runCommand("setblock -3 100 8 minecraft:jack_o_lantern[facing=north]");
			server.runCommand("setblock 4 100 9 minecraft:dark_oak_log");
			server.runCommand("setblock 4 101 9 minecraft:dark_oak_log");
			// close-up
			server.runCommand("summon horrormod:mysterious_witch 0.5 100 3.2 {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]}");
			server.runCommand("tp @a 0.5 100.3 0.6 0 12");
			sp.getConnection().waitForChunksRender();
			context.waitTicks(200);
			save(context, "mysterious_witch_closeup", out);
			// throwing: survival player hits her, she gets angry and throws splash potions back
			server.runCommand("kill @e[type=horrormod:mysterious_witch]");
			server.runCommand("gamemode survival @a");
			server.runCommand("effect give @a minecraft:resistance 100 4 true");
			server.runCommand("xp set @a 0 levels");
			server.runCommand("xp set @a 0 points");
			server.runCommand("tp @a 0.5 100 0 0 0");
			server.runCommand("summon horrormod:mysterious_witch 0.5 100 6 {PersistenceRequired:1b,Rotation:[180f,0f]}");
			context.waitTicks(5);
			server.runOnServer(s -> {
				var level = s.overworld();
				var player = s.getPlayerList().getPlayers().get(0);
				for (MysteriousWitch w : level.getEntities(ModEntities.MYSTERIOUS_WITCH, e -> true)) {
					w.hurtServer(level, level.damageSources().playerAttack(player), 1.0F);
				}
			});
			// wait until a potion is in the air
			for (int i = 0; i < 120; i++) {
				context.waitTick();
				boolean flying = server.computeOnServer(s -> !s.overworld().getEntities(net.minecraft.world.entity.EntityTypes.SPLASH_POTION, e -> true).isEmpty());
				if (flying) {
					break;
				}
			}
			context.waitTicks(3);
			save(context, "mysterious_witch_throwing", out);
			// inventory: drinkable + splash of each of the 4 potions
			server.runCommand("kill @e[type=horrormod:mysterious_witch]");
			server.runCommand("effect clear @a");
			server.runCommand("clear @a");
			server.runCommand("gamemode creative @a");
			for (String id : IDS) {
				server.runCommand("give @a minecraft:potion[potion_contents={potion:\"horrormod:" + id + "\"}]");
			}
			for (String id : IDS) {
				server.runCommand("give @a minecraft:splash_potion[potion_contents={potion:\"horrormod:" + id + "\"}]");
			}
			server.runCommand("gamemode survival @a");
			context.waitTicks(10);
			context.setScreen(() -> new net.minecraft.client.gui.screens.inventory.InventoryScreen(net.minecraft.client.Minecraft.getInstance().player));
			context.waitTicks(10);
			save(context, "mysterious_witch_potions_inventory", out);
			context.setScreen(() -> null);
		}
	}

	private static void save(ClientGameTestContext context, String name, Path out) {
		Path shot = context.takeScreenshot(TestScreenshotOptions.of(name).disableCounterPrefix());
		try {
			Files.createDirectories(out);
			Files.copy(shot, out.resolve(name + ".png"), StandardCopyOption.REPLACE_EXISTING);
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
	}
}

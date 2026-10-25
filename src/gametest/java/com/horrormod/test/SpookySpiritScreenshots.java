package com.horrormod.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;

/**
 * Dev-only: the Spooky Spirit floating in front of a pumpkin patch and colorful blocks so you can see them THROUGH it,
 * plus the "wooo" subtitle. Not part of the mod jar and not run by ./gradlew build.
 * Run with: DISPLAY=:77 LIBGL_ALWAYS_SOFTWARE=1 GALLIUM_DRIVER=llvmpipe SDL_VIDEO_FORCE_EGL=1 ALSOFT_DRIVERS=null ./gradlew runClientGameTest -PclientShots
 */
public class SpookySpiritScreenshots implements FabricClientGameTest {
	private static final List<String> SETUP = List.of("gamerule send_command_feedback false", "gamemode creative @a", "weather clear",
			"gamerule advance_time false", "gamerule advance_weather false", "gamerule spawn_mobs false");

	@Override
	public void runTest(ClientGameTestContext context) {
		Path out = Path.of(System.getProperty("horrormod.shots", "build/spirit_shots"));
		context.getInput().resizeWindow(1280, 720);
		context.runOnClient(c -> c.options.showSubtitles().set(true));
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			var server = sp.getServer();
			sp.getConnection().waitForChunksRender();
			SETUP.forEach(server::runCommand);
			server.runCommand("time set 6000");
			server.runCommand("tp @a 0.5 100 -20");
			server.runCommand("fill -12 99 -6 12 99 16 minecraft:grass_block");
			server.runCommand("fill -12 100 -6 12 112 16 minecraft:air");
			// a colorful wall behind the spirit: stripes you can see through its clear body
			server.runCommand("fill -3 100 9 -2 104 9 minecraft:orange_wool");
			server.runCommand("fill -1 100 9 0 104 9 minecraft:purple_wool");
			server.runCommand("fill 1 100 9 2 104 9 minecraft:lime_wool");
			server.runCommand("fill 3 100 9 4 104 9 minecraft:orange_wool");
			server.runCommand("setblock -2 100 7 minecraft:carved_pumpkin[facing=north]");
			server.runCommand("setblock 3 100 7 minecraft:jack_o_lantern[facing=north]");
			server.runCommand("setblock 0 100 7 minecraft:pumpkin");
			server.runCommand("summon horrormod:spooky_spirit 0.5 100.8 6.0 {NoAI:1b,PersistenceRequired:1b,NoGravity:1b,Rotation:[180f,0f]}");
			server.runCommand("summon horrormod:spooky_spirit 2.6 101.6 7.0 {NoAI:1b,PersistenceRequired:1b,NoGravity:1b,Rotation:[160f,0f]}");
			server.runCommand("summon horrormod:spooky_spirit -2.0 101.2 6.8 {PersistenceRequired:1b,Rotation:[200f,0f]}");
			server.runCommand("tp @a 0.5 101 1.5 0 6");
			sp.getConnection().waitForChunksRender();
			context.waitTicks(40);
			save(context, "spooky_spirit_ingame", out);
			server.runCommand("tp @a 0.5 101.3 3.6 0 8");
			context.waitTicks(20);
			save(context, "spooky_spirit_ingame_closeup", out);
			server.runCommand("tp @a 4.5 101.5 3.0 facing 0.5 101.4 6.0");
			context.waitTicks(20);
			save(context, "spooky_spirit_ingame_side", out);
			// spooky night version
			server.runCommand("time set 18000");
			server.runCommand("tp @a 0.5 101 1.5 0 6");
			context.waitTicks(30);
			save(context, "spooky_spirit_ingame_night", out);
			// wooo subtitle: play the spirit's own sound right next to the player
			server.runCommand("playsound horrormod:entity.spooky_spirit.wooo neutral @a 0.5 101 5.0 1 1");
			context.waitTicks(8);
			save(context, "spooky_spirit_ingame_wooo_subtitle", out);
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

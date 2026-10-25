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
 * Dev-only: the Super Phantom flying in the night sky, a close-up, and a before/after of it snatching one diamond
 * (with the "Super Phantom snatches an item" subtitle). Not part of the mod jar and not run by ./gradlew build.
 * Run with: DISPLAY=:77 LIBGL_ALWAYS_SOFTWARE=1 GALLIUM_DRIVER=llvmpipe SDL_VIDEO_FORCE_EGL=1 ALSOFT_DRIVERS=null ./gradlew runClientGameTest -PclientShots
 */
public class SuperPhantomScreenshots implements FabricClientGameTest {
	private static final List<String> SETUP = List.of("gamerule send_command_feedback false", "gamemode creative @a", "weather clear",
			"gamerule advance_time false", "gamerule advance_weather false", "gamerule spawn_mobs false", "gamerule announce_advancements false", "advancement grant @a everything", "recipe give @a *");

	@Override
	public void runTest(ClientGameTestContext context) {
		Path out = Path.of(System.getProperty("horrormod.shots", "build/super_phantom_shots"));
		context.getInput().resizeWindow(1280, 720);
		context.runOnClient(c -> c.options.showSubtitles().set(true));
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			var server = sp.getServer();
			sp.getConnection().waitForChunksRender();
			SETUP.forEach(server::runCommand);
			server.runCommand("time set 13000"); // dusk, so you can see it against the sky
			server.runCommand("tp @a 0.5 100 -20");
			server.runCommand("fill -16 99 -10 16 99 20 minecraft:grass_block");
			server.runCommand("fill -16 100 -10 16 120 20 minecraft:air");
			server.runCommand("setblock -3 100 8 minecraft:jack_o_lantern[facing=north]");
			server.runCommand("setblock 4 100 10 minecraft:jack_o_lantern[facing=north]");
			// flying: two Super Phantoms swooping around in the sky (AI on, but no players to snatch from in creative)
			server.runCommand("summon horrormod:super_phantom 0.5 104 7 {PersistenceRequired:1b}");
			server.runCommand("summon horrormod:super_phantom -4 108 14 {PersistenceRequired:1b}");
			server.runCommand("tp @a 0.5 100 -2 0 -22");
			sp.getConnection().waitForChunksRender();
			context.waitTicks(200); // let toasts fade
			save(context, "super_phantom_ingame_flying", out);
			// close-up: a still one right in front of you
			server.runCommand("kill @e[type=horrormod:super_phantom]");
			server.runCommand("summon horrormod:super_phantom 0.5 101.0 4.0 {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]}");
			server.runCommand("tp @a 0.5 101.2 -0.5 0 8");
			context.waitTicks(30);
			save(context, "super_phantom_ingame_closeup", out);
			// before / after: survival, 5 diamonds in the hotbar, a phantom right next to you that is still on cooldown
			server.runCommand("kill @e[type=horrormod:super_phantom]");
			server.runCommand("clear @a");
			server.runCommand("gamemode survival @a");
			server.runCommand("effect give @a minecraft:resistance 100 4 true");
			server.runCommand("give @a minecraft:diamond 5");
			server.runCommand("xp set @a 0 levels");
			server.runCommand("xp set @a 0 points");
			server.runCommand("tp @a 0.5 100 0.6 0 10");
			// camera behind you, so you can see the phantom right in front of you and your hotbar
			context.runOnClient(c -> c.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK));
			server.runCommand("summon horrormod:super_phantom 0.5 101.0 2.1 {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f],SnatchCooldown:80}");
			context.waitTicks(30);
			save(context, "super_phantom_snatch_before", out);
			context.waitTicks(56);
			save(context, "super_phantom_snatch_after", out);
			// attacking (0.3.0, hostile): at night a free-flying one swoops in and bites
			server.runCommand("kill @e[type=horrormod:super_phantom]");
			server.runCommand("kill @e[type=item]");
			server.runCommand("effect clear @a");
			server.runCommand("time set 18000");
			server.runCommand("tp @a 0.5 100 0.5 0 -10");
			server.runCommand("summon horrormod:super_phantom 0.5 104 7 {PersistenceRequired:1b,SnatchCooldown:2000}");
			context.waitTicks(50);
			save(context, "super_phantom_attacking", out);
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

package com.horrormod.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;

/** Dev-only (0.5.0): the Spirit item in the inventory/hand, and a Spirit dropped on the ground next to a Ghost. */
public class SpiritScreenshots implements FabricClientGameTest {
	private static final List<String> SETUP = List.of("gamerule send_command_feedback false", "gamemode creative @a", "weather clear",
			"gamerule advance_time false", "gamerule advance_weather false", "gamerule spawn_mobs false", "gamerule announce_advancements false");

	@Override
	public void runTest(ClientGameTestContext context) {
		Path out = Path.of(System.getProperty("horrormod.shots", "build/spirit_shots"));
		context.getInput().resizeWindow(1280, 720);
		context.runOnClient(c -> c.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			var server = sp.getServer();
			sp.getConnection().waitForChunksRender();
			SETUP.forEach(server::runCommand);
			server.runCommand("time set 6000");
			server.runCommand("tp @a 0.5 100 -20");
			server.runCommand("fill -16 99 -10 16 99 20 minecraft:grass_block");
			server.runCommand("fill -16 100 -10 16 120 20 minecraft:air");
			// ground: a dropped Spirit next to a Ghost
			server.runCommand("summon horrormod:spooky_spirit 1.3 100.6 3.5 {NoAI:1b,PersistenceRequired:1b,Rotation:[180f,0f]}");
			server.runCommand("summon minecraft:item -0.2 100 2.4 {Item:{id:\"horrormod:spirit\",count:1},PickupDelay:32767s,Age:-32768s}");
			server.runCommand("tp @a 0.5 100.6 0.2 0 25");
			sp.getConnection().waitForChunksRender();
			context.waitTicks(100);
			save(context, "spirit_dropped_next_to_ghost", out);
			// inventory + in hand
			server.runCommand("kill @e[type=!minecraft:player]");
			server.runCommand("clear @a");
			server.runCommand("give @a horrormod:spirit 1");
			server.runCommand("give @a horrormod:spirit 16");
			server.runCommand("gamemode survival @a");
			server.runCommand("tp @a 0.5 100 0.2 0 0");
			context.waitTicks(20);
			save(context, "spirit_in_hand", out);
			context.setScreen(() -> new net.minecraft.client.gui.screens.inventory.InventoryScreen(net.minecraft.client.Minecraft.getInstance().player));
			context.waitTicks(10);
			save(context, "spirit_inventory", out);
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

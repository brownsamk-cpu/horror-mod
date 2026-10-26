package com.horrormod.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.horrormod.castle.CastleLevelSix;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Dev-only (0.7.1): real in-game pictures of the stacked castle tower (Level 7 on top of Level 6). */
public class CastleTowerScreenshots implements FabricClientGameTest {
	private static final List<String> SETUP = List.of("gamerule send_command_feedback false", "gamemode creative @a", "weather clear", "difficulty peaceful",
			"gamerule advance_time false", "gamerule advance_weather false", "gamerule spawn_mobs false", "gamerule announce_advancements false");

	@Override
	public void runTest(ClientGameTestContext context) {
		Path out = Path.of(System.getProperty("horrormod.castleShots", "/workspace/mcmod/horror-mod/build/castle_shots"));
		context.getInput().resizeWindow(1280, 720);
		context.runOnClient(c -> c.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			TestServerContext server = sp.getServer();
			sp.getConnection().waitForChunksRender();
			SETUP.forEach(server::runCommand);
			server.runCommand("time set 6000");
			server.runCommand("tp @a 0.5 110 0");
			context.waitTicks(40);
			// facing north: Level 6 from z = 2 to z = -47 (bottom y = 100), Level 7 stacked on top (bottom y = 121)
			server.runOnServer(s -> CastleLevelSix.buildTower(s.overworld(), new BlockPos(-6, 100, 2), Direction.NORTH));
			server.runCommand("effect give @a minecraft:night_vision infinite 0 true");
			context.waitTicks(20);
			server.runCommand("gamemode spectator @a");
			shot(context, sp, server, "tp @a 52 124 -4 110 6", "castle_tower_outside_stacked", out);
			// Level 6's last room: the gold 7, the Go UP sign and the green arrow pads
			shot(context, sp, server, "tp @a -1.5 117.5 -41.5 -150 38", "castle_tower_go_up_pad", out);
			// step on the pad for real: whoosh, up to Level 7's start
			server.runCommand("gamemode creative @a");
			server.runCommand("tp @a 3.5 114 -43.5 180 10");
			context.waitTicks(40);
			sp.getConnection().waitForChunksRender();
			context.waitTicks(30);
			save(context, "castle_tower_arrived_level7", out);
		}
	}

	private static void shot(ClientGameTestContext context, TestSingleplayerContext sp, TestServerContext server, String tp, String name, Path out) {
		server.runCommand(tp);
		context.waitTicks(5);
		sp.getConnection().waitForChunksRender();
		context.waitTicks(40);
		save(context, name, out);
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

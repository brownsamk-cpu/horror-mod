package com.horrormod.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.horrormod.castle.CastleLevelSix;
import com.horrormod.castle.CastleRides;
import com.horrormod.castle.LevelSevenGateBlock;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Dev-only (0.6.0): real in-game pictures of Castle Level 6. */
public class CastleScreenshots implements FabricClientGameTest {
	private static final List<String> SETUP = List.of("gamerule send_command_feedback false", "gamemode creative @a", "weather clear", "difficulty normal",
			"gamerule advance_time false", "gamerule advance_weather false", "gamerule spawn_mobs false", "gamerule announce_advancements false");

	@Override
	public void runTest(ClientGameTestContext context) {
		Path out = Path.of(System.getProperty("horrormod.castleShots", "/workspace/mcmod/horror-mod/build/castle_shots"));
		context.getInput().resizeWindow(1280, 720);
		context.runOnClient(c -> c.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON));
		try (TestSingleplayerContext sp = context.worldBuilder().create()) {
			var server = sp.getServer();
			sp.getConnection().waitForChunksRender();
			SETUP.forEach(server::runCommand);
			server.runCommand("time set 6000");
			server.runCommand("tp @a 0.5 110 0");
			context.waitTicks(40);
			server.runCommand("fill -24 99 -45 24 99 26 minecraft:grass_block");
			for (int z = -45; z <= 26; z += 12) {
				server.runCommand("fill -24 100 " + z + " 24 128 " + Math.min(26, z + 11) + " minecraft:air");
			}
			server.runOnServer(s -> CastleLevelSix.build(s.overworld(), new BlockPos(-6, 100, 10), Direction.NORTH));
			server.runCommand("effect give @a minecraft:night_vision infinite 0 true");

			server.runCommand("gamemode spectator @a");
			shot(context, sp, server, "tp @a 0.5 134 26 180 38", "castle_level6_overview", out);
			shot(context, sp, server, "tp @a 0.5 111 5.5 180 12", "castle_level6_vines", out);
			server.runCommand("gamemode creative @a");
			shot(context, sp, server, "tp @a 3.5 101 7.5 165 -38", "castle_level6_climbing", out);
			shot(context, sp, server, "tp @a 0.5 114 -15.5 0 8", "castle_level6_strays", out);
			server.runCommand("give @a horrormod:zip_line_hook");
			shot(context, sp, server, "tp @a 0.5 114 -15.5 180 -12", "castle_level6_zip_line", out);
			server.runCommand("tp @a 0.5 114 -16.5 180 -5");
			context.waitTicks(10);
			server.runOnServer(s -> CastleRides.startZipLine(s.getPlayerList().getPlayers().get(0)));
			context.waitTicks(5);
			save(context, "castle_level6_zip_line_riding", out);
			context.waitTicks(30);
			server.runCommand("clear @a");
			server.runCommand("give @a horrormod:level_seven_key");
			shot(context, sp, server, "tp @a 0.5 114 -27.5 180 4", "castle_level6_key_and_gate", out);
			server.runOnServer(s -> LevelSevenGateBlock.open(s.overworld(), new BlockPos(0, 115, -34)));
			context.waitTicks(30);
			shot(context, sp, server, "tp @a 0.5 114 -31.5 180 10", "castle_level6_gate_open_level7", out);
		}
	}

	private static void shot(ClientGameTestContext context, TestSingleplayerContext sp, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
			String tp, String name, Path out) {
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

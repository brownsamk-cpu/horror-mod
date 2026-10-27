package com.horrormod.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.horrormod.castle.CastleLevelNine;
import com.horrormod.castle.HiddenButtonBlock;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Dev-only (0.7.0): real in-game pictures of Castle Level 9 (0.8.0). */
public class CastleLevelNineScreenshots implements FabricClientGameTest {
	private static final List<String> SETUP = List.of("gamerule send_command_feedback false", "gamemode creative @a", "weather clear", "difficulty easy",
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
			// facing north: the front door is at z = 8, the secret door at z = -4; floor at y = 100
			var built = new CastleLevelNine.Built[1];
			server.runOnServer(s -> built[0] = CastleLevelNine.build(s.overworld(), new BlockPos(-7, 100, 8), Direction.NORTH));
			server.runCommand("effect give @a minecraft:night_vision infinite 0 true");
			context.waitTicks(20);
			server.runCommand("gamemode spectator @a");
			shot(context, sp, server, "tp @a 0.5 106.5 7.5 180 32", "castle_level9_room", out);
			// button 3 on a bookshelf (local 12,2,6 = world 5,102,2)
			shot(context, sp, server, "tp @a 2.5 102 2.5 -90 20", "castle_level9_hidden_button", out);
			server.runOnServer(s -> {
				var player = s.getPlayerList().getPlayers().get(0);
				for (BlockPos b : built[0].buttons()) {
					var st = s.overworld().getBlockState(b);
					((HiddenButtonBlock) st.getBlock()).press(st, s.overworld(), b, player);
				}
			});
			context.waitTicks(10);
			shot(context, sp, server, "tp @a 0.5 102 3.5 180 8", "castle_level9_secret_door_open", out);
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

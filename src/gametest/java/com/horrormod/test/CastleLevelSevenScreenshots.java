package com.horrormod.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.horrormod.castle.CastleLevelSeven;
import com.horrormod.castle.GrapplingHookItem;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Dev-only (0.7.0): real in-game pictures of Castle Level 7.. */
public class CastleLevelSevenScreenshots implements FabricClientGameTest {
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
			// facing north: the start is at z = 9, the end at z = -31; start floor at y = 100
			server.runOnServer(s -> CastleLevelSeven.build(s.overworld(), new BlockPos(-6, 88, 10), Direction.NORTH));
			server.runCommand("effect give @a minecraft:night_vision infinite 0 true");
			context.waitTicks(20);

			server.runCommand("gamemode spectator @a");
			shot(context, sp, server, "tp @a 0.5 121 13 180 42", "castle_level7_overview", out);
			shot(context, sp, server, "tp @a -4.5 104 -6 -120 28", "castle_level7_electra_cores_over_lava", out);
			server.runCommand("gamemode creative @a");
			server.runCommand("clear @a");
			server.runCommand("give @a horrormod:grappling_hook");
			// stand on the island and grapple the target on the far side
			server.runCommand("tp @a 3.5 101 -11.5 180 5");
			context.waitTicks(5);
			sp.getConnection().waitForChunksRender();
			context.waitTicks(30);
			// aim at the grapple target at local (9, 12, 30) = world (3, 100, -20)
			server.runCommand("tp @a 3.5 101 -11.5 180 6");
			context.waitTicks(10);
			server.runOnServer(s -> GrapplingHookItem.grapple(s.getPlayerList().getPlayers().get(0)));
			context.waitTicks(3);
			save(context, "castle_level7_grappling_hook", out);
			context.waitTicks(30);
			server.runCommand("gamemode spectator @a");
			shot(context, sp, server, "tp @a 0.5 104 -21 180 25", "castle_level7_level8_sign", out);
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

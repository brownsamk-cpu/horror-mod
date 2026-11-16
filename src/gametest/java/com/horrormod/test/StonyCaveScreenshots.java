package com.horrormod.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.horrormod.cave.StoneDoorBlock;
import com.horrormod.cave.StonyCave;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Dev-only: real in-game pictures of the Stony Cave and the Stone Monster (0.9.0). */
public class StonyCaveScreenshots implements FabricClientGameTest {
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
			server.runCommand("tp @a 0.5 110 20");
			context.waitTicks(40);
			// facing north from (-10,100,8): the monster stands at world (0.5, 101, 0.5), the door at z = -6
			var built = new StonyCave.Built[1];
			server.runOnServer(s -> {
				built[0] = StonyCave.build(s.overworld(), new BlockPos(-10, 100, 8), Direction.NORTH);
				built[0].monster().stopTargetingForTest();
			});
			context.waitTicks(20);
			server.runCommand("gamemode spectator @a");
			shot(context, sp, server, "tp @a 0.5 102.6 7.2 180 -8", "stony_cave_hall_dim", out);
			server.runCommand("effect give @a minecraft:night_vision infinite 0 true");
			shot(context, sp, server, "tp @a 4.5 102.5 4.5 135 -12", "stony_cave_stone_monster", out);
			// mid-throw and mid-slam
			server.runOnServer(s -> built[0].monster().startSlam());
			context.waitTicks(9);
			save(context, "stony_cave_monster_slam", out);
			context.waitTicks(40);
			server.runOnServer(s -> {
				var z = net.minecraft.world.entity.EntityTypes.ZOMBIE.create(s.overworld(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
				z.snapTo(0.5, 101, 12.5, 0, 0);
				z.setNoAi(true);
				s.overworld().addFreshEntity(z);
				built[0].monster().performRangedAttack(z, 1.0F);
			});
			context.waitTicks(5);
			save(context, "stony_cave_monster_throw", out);
			server.runOnServer(s -> {
				built[0].monster().discard();
				StoneDoorBlock.open(s.overworld(), built[0].door().get(0));
			});
			shot(context, sp, server, "tp @a 0.5 102.5 -2.5 180 10", "stony_cave_door_open", out);
			shot(context, sp, server, "tp @a -7 103 -8.5 180 20", "stony_cave_snowy_room", out);
			shot(context, sp, server, "tp @a 0.5 103 -8.5 180 20", "stony_cave_jungle_room", out);
			shot(context, sp, server, "tp @a 7.5 103.5 -8.5 180 20", "stony_cave_desert_room", out);
			shot(context, sp, server, "tp @a 0.5 108 30 180 15", "stony_cave_outside", out);
		}
	}

	private static void shot(ClientGameTestContext context, TestSingleplayerContext sp, TestServerContext server, String tp, String name, Path out) {
		server.runCommand(tp);
		context.waitTicks(5);
		sp.getConnection().waitForChunksRender();
		context.waitTicks(30);
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

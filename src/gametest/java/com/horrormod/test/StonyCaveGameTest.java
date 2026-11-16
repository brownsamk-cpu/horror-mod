package com.horrormod.test;

import com.horrormod.cave.CaveBlocks;
import com.horrormod.cave.CaveEntities;
import com.horrormod.cave.CaveItems;
import com.horrormod.cave.StoneMonster;
import com.horrormod.cave.StonyCave;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Horror Mod 0.9.0: the Stony Cave, the Stone Monster mini boss, the Stone Key and the Stone Door (June and Leon's design). */
public class StonyCaveGameTest {
	private static StoneMonster calmMonster(GameTestHelper helper, BlockPos rel) {
		StoneMonster m = helper.spawn(CaveEntities.STONE_MONSTER, rel);
		m.stopTargetingForTest();
		return m;
	}

	/** A stone pad high in the sky over this test, so thrown rocks and slams can't reach the other tests' mobs. */
	private static BlockPos skyPad(GameTestHelper helper) {
		BlockPos base = helper.absolutePos(new BlockPos(1, 1, 1)).above(100);
		for (BlockPos p : BlockPos.betweenClosed(base.offset(-4, 0, -4), base.offset(4, 0, 12))) {
			helper.getLevel().setBlock(p, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
		return base.above();
	}

	private static void clearPad(GameTestHelper helper, BlockPos top) {
		BlockPos base = top.below();
		helper.getLevel().getEntitiesOfClass(Entity.class, AABB.encapsulatingFullBlocks(base.offset(-12, -2, -12), base.offset(12, 10, 24)),
				e -> !(e instanceof ServerPlayer)).forEach(Entity::discard);
		for (BlockPos p : BlockPos.betweenClosed(base.offset(-4, 0, -4), base.offset(4, 0, 12))) {
			helper.getLevel().setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	private static void moveTo(Entity e, BlockPos pos) {
		e.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
	}

	/** Registered as horrormod:stone_monster, ~5 blocks tall, 150 health, has a spawn egg. */
	@GameTest
	public void stoneMonsterSpawns(GameTestHelper helper) {
		StoneMonster m = calmMonster(helper, new BlockPos(1, 2, 1));
		helper.assertTrue(m.isAlive(), "should spawn");
		helper.assertTrue(BuiltInRegistries.ENTITY_TYPE.getKey(CaveEntities.STONE_MONSTER).toString().equals("horrormod:stone_monster"), "id");
		helper.assertTrue(m.getType().getDescriptionId().equals("entity.horrormod.stone_monster"), "lang key");
		helper.assertTrue(Math.abs(m.getBbHeight() - 5.0F) < 0.01F, "5 blocks tall");
		helper.assertTrue(m.getMaxHealth() == StoneMonster.MAX_HEALTH && StoneMonster.MAX_HEALTH == 150.0F, "150 health");
		helper.assertTrue(CaveItems.STONE_MONSTER_SPAWN_EGG instanceof SpawnEggItem, "spawn egg");
		helper.assertTrue(BuiltInRegistries.ITEM.getKey(CaveItems.STONY_CAVE).toString().equals("horrormod:stony_cave"), "stony cave id");
		helper.assertTrue(BuiltInRegistries.ITEM.getKey(CaveItems.STONE_KEY).toString().equals("horrormod:stone_key"), "stone key id");
		helper.assertTrue(BuiltInRegistries.BLOCK.getKey(CaveBlocks.STONE_DOOR).toString().equals("horrormod:stone_door"), "stone door id");
		m.discard();
		helper.succeed();
	}

	/** Defeating the Stone Monster always drops the Stone Key. */
	@GameTest(maxTicks = 40)
	public void stoneMonsterDropsKey(GameTestHelper helper) {
		StoneMonster m = calmMonster(helper, new BlockPos(1, 2, 1));
		m.kill(helper.getLevel());
		helper.succeedWhen(() -> {
			var items = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds().inflate(3));
			helper.assertTrue(items.stream().anyMatch(i -> i.getItem().is(CaveItems.STONE_KEY)), "should drop the Stone Key");
		});
	}

	/** The Stone Key opens the whole 3x3 Stone Door; without it the door stays shut. */
	@GameTest(maxTicks = 20)
	@SuppressWarnings("removal")
	public void stoneKeyOpensDoor(GameTestHelper helper) {
		for (int x = 1; x <= 3; x++) {
			for (int y = 1; y <= 3; y++) {
				helper.setBlock(new BlockPos(x, y, 2), CaveBlocks.STONE_DOOR);
			}
		}
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		BlockPos door = helper.absolutePos(new BlockPos(2, 2, 2));
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(door), Direction.SOUTH, door, false);
		var state = helper.getLevel().getBlockState(door);
		state.useItemOn(new ItemStack(Items.STICK), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
		state.useItemOn(new ItemStack(com.horrormod.castle.CastleItems.LEVEL_SEVEN_KEY), helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
		helper.assertBlockPresent(CaveBlocks.STONE_DOOR, new BlockPos(2, 2, 2));
		ItemStack key = new ItemStack(CaveItems.STONE_KEY);
		player.setItemInHand(InteractionHand.MAIN_HAND, key);
		state.useItemOn(key, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
		for (int x = 1; x <= 3; x++) {
			for (int y = 1; y <= 3; y++) {
				helper.assertBlockPresent(Blocks.AIR, new BlockPos(x, y, 2));
			}
		}
		helper.assertTrue(player.getMainHandItem().is(CaveItems.STONE_KEY), "you keep the key");
		helper.succeed();
	}

	/** It winds up and throws a rock at its target (released partway through the throw animation). */
	@GameTest(maxTicks = 60)
	public void stoneMonsterThrowsRocks(GameTestHelper helper) {
		BlockPos pad = skyPad(helper);
		StoneMonster m = calmMonster(helper, new BlockPos(1, 2, 1));
		Zombie z = helper.spawn(EntityTypes.ZOMBIE, new BlockPos(1, 2, 3));
		z.setNoAi(true);
		moveTo(m, pad);
		moveTo(z, pad.south(7));
		m.performRangedAttack(z, 1.0F);
		helper.assertTrue(m.getRocksThrown() == 0, "rock not released during the wind-up");
		helper.succeedWhen(() -> {
			helper.assertTrue(m.getRocksThrown() == 1, "threw one rock");
			clearPad(helper, pad);
		});
	}

	/** The ground slam hurts something standing close and knocks it back. */
	@GameTest(maxTicks = 60)
	public void stoneMonsterGroundSlam(GameTestHelper helper) {
		BlockPos pad = skyPad(helper);
		StoneMonster m = calmMonster(helper, new BlockPos(2, 2, 2));
		Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(1, 2, 1));
		moveTo(m, pad);
		moveTo(pig, pad.south(3));
		m.startSlam();
		helper.succeedWhen(() -> {
			helper.assertTrue(m.getSlams() == 1, "slammed");
			helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "the slam hit the pig");
			clearPad(helper, pad);
		});
	}

	/** The boss bar shows to a nearby player, follows its health, and goes away when you walk far off. */
	@GameTest(maxTicks = 40)
	@SuppressWarnings("removal")
	public void bossBarShowsNearby(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		StoneMonster m = calmMonster(helper, new BlockPos(1, 2, 1));
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.snapTo(m.getX() + 4, m.getY(), m.getZ(), 0, 0);
		m.updateBossBarPlayers(level);
		helper.assertTrue(m.getBossEvent().getPlayers().contains(player), "boss bar shows when near");
		m.setHealth(75.0F);
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(Math.abs(m.getBossEvent().getProgress() - 0.5F) < 0.02F, "bar shows half health");
			player.snapTo(m.getX() + StoneMonster.BOSS_BAR_RANGE + 20, m.getY(), m.getZ(), 0, 0);
			m.updateBossBarPlayers(level);
			helper.assertTrue(!m.getBossEvent().getPlayers().contains(player), "boss bar hides when far");
			m.discard();
			helper.assertTrue(m.getBossEvent().getPlayers().isEmpty(), "no bar after it's gone");
			helper.succeed();
		});
	}

	/** The Stony Cave item's builder: Stone Door, Stone Monster guarding it, three themed rooms with reward chests, dim and cobwebby. */
	@GameTest(maxTicks = 40)
	@SuppressWarnings("removal")
	public void stonyCavePlacesDoorAndRooms(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = helper.absolutePos(new BlockPos(0, 1, 0)).above(170);
		Direction fwd = Direction.WEST;
		StonyCave.Built built = StonyCave.build(level, origin, fwd);
		try {
			StoneMonster m = built.monster();
			helper.assertTrue(m != null && m.isAlive() && m.isPersistenceRequired(), "the Stone Monster is there and stays");
			m.stopTargetingForTest();
			helper.assertTrue(built.door().size() == 9, "3x3 door");
			for (BlockPos d : built.door()) {
				helper.assertTrue(level.getBlockState(d).is(CaveBlocks.STONE_DOOR), "stone door at " + d);
			}
			AABB hall = AABB.encapsulatingFullBlocks(StonyCave.toWorld(origin, fwd, StonyCave.HALL_X0, 1, StonyCave.HALL_Z0),
					StonyCave.toWorld(origin, fwd, StonyCave.HALL_X1, StonyCave.HALL_Y1, StonyCave.HALL_Z1));
			helper.assertTrue(hall.contains(m.position()), "the monster guards the entrance hall");
			helper.assertTrue(m.getY() + m.getBbHeight() <= hall.maxY + 0.01, "it fits under the ceiling");
			helper.assertTrue(built.chests().size() == 3, "3 chests");
			for (BlockPos c : built.chests()) {
				helper.assertTrue(level.getBlockEntity(c) instanceof ChestBlockEntity ch && StonyCave.hasReward(ch), "reward chest at " + c);
			}
			// themes
			helper.assertTrue(count(level, origin, fwd, Blocks.SNOW_BLOCK, 1, 5) > 0 && count(level, origin, fwd, Blocks.SPRUCE_LEAVES, 1, 5) > 0
					&& count(level, origin, fwd, Blocks.PACKED_ICE, 1, 5) > 0, "snowy room");
			helper.assertTrue(count(level, origin, fwd, Blocks.MOSS_BLOCK, 8, 12) > 0 && count(level, origin, fwd, Blocks.JUNGLE_LEAVES, 8, 12) > 0
					&& count(level, origin, fwd, Blocks.VINE, 8, 12) > 0, "jungle room");
			helper.assertTrue(count(level, origin, fwd, Blocks.SAND, 15, 19) > 0 && count(level, origin, fwd, Blocks.SANDSTONE, 14, 20) > 0
					&& count(level, origin, fwd, Blocks.CACTUS, 15, 19) > 0, "desert room");
			// 50 percent scary: dim (just two torches), cobwebs, glowing lichen
			int torches = 0, webs = 0;
			for (BlockPos p : BlockPos.betweenClosed(StonyCave.toWorld(origin, fwd, 0, 0, 0), StonyCave.toWorld(origin, fwd, StonyCave.WIDTH - 1, StonyCave.HEIGHT - 1, 15))) {
				torches += level.getBlockState(p).is(Blocks.TORCH) ? 1 : 0;
				webs += level.getBlockState(p).is(Blocks.COBWEB) ? 1 : 0;
			}
			helper.assertTrue(torches == 2, "only two torches in the hall, got " + torches);
			helper.assertTrue(webs >= 8, "cobwebs in the corners");
			// open it with the key and the way to the rooms is clear
			ServerPlayer player = helper.makeMockServerPlayerInLevel();
			BlockPos d0 = built.door().get(4);
			ItemStack key = new ItemStack(CaveItems.STONE_KEY);
			player.setItemInHand(InteractionHand.MAIN_HAND, key);
			level.getBlockState(d0).useItemOn(key, level, player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(d0), Direction.UP, d0, false));
			for (BlockPos d : built.door()) {
				helper.assertTrue(level.getBlockState(d).isAir(), "door open at " + d);
			}
			helper.assertTrue(level.getBlockState(StonyCave.toWorld(origin, fwd, 10, 1, 15)).canBeReplaced()
					&& level.getBlockState(StonyCave.toWorld(origin, fwd, 10, 2, 17)).isAir(), "passage to the rooms");
			player.discard();
		} finally {
			clear(level, origin, fwd);
		}
		helper.succeed();
	}

	private static int count(ServerLevel level, BlockPos origin, Direction fwd, Block block, int x0, int x1) {
		int n = 0;
		for (int x = x0; x <= x1; x++) {
			for (int y = 0; y <= StonyCave.ROOM_Y1 + 1; y++) {
				for (int z = StonyCave.ROOM_Z0 - 1; z <= StonyCave.ROOM_Z1 + 1; z++) {
					n += level.getBlockState(StonyCave.toWorld(origin, fwd, x, y, z)).is(block) ? 1 : 0;
				}
			}
		}
		return n;
	}

	private static void clear(ServerLevel level, BlockPos origin, Direction fwd) {
		BlockPos a = StonyCave.toWorld(origin, fwd, 0, 0, 0), b = StonyCave.toWorld(origin, fwd, StonyCave.WIDTH - 1, StonyCave.HEIGHT - 1, StonyCave.LENGTH - 1);
		level.getEntitiesOfClass(Entity.class, AABB.encapsulatingFullBlocks(a, b).inflate(3), e -> !(e instanceof ServerPlayer)).forEach(Entity::discard);
		for (BlockPos p : BlockPos.betweenClosed(a, b)) {
			level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
		}
	}
}

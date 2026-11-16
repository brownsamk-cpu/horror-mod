package com.horrormod.cave;

import com.horrormod.HorrorMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;

/** 0.9.0: the Stony Cave (June and Leon's design). */
public final class CaveItems {
	/** Use it and the Stony Cave appears right in front of you, with the Stone Monster guarding the Stone Door. */
	public static final Item STONY_CAVE = register("stony_cave", new StonyCaveItem(props("stony_cave").stacksTo(1).rarity(Rarity.RARE)));
	/** Dropped by the Stone Monster; opens the Stone Door. */
	public static final Item STONE_KEY = register("stone_key", new Item(props("stone_key").stacksTo(1).rarity(Rarity.UNCOMMON)));
	/** What the Stone Monster throws (also what its rocks look like). */
	public static final Item STONE_ROCK = register("stone_rock", new Item(props("stone_rock")));
	public static final Item STONE_MONSTER_SPAWN_EGG = register("stone_monster_spawn_egg",
			new SpawnEggItem(props("stone_monster_spawn_egg").spawnEgg(CaveEntities.STONE_MONSTER)));
	public static final Item STONE_DOOR = register("stone_door", new BlockItem(CaveBlocks.STONE_DOOR, props("stone_door").useBlockDescriptionPrefix()));

	private CaveItems() {
	}

	private static Item.Properties props(final String name) {
		return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, HorrorMod.id(name)));
	}

	private static Item register(final String name, final Item item) {
		return Registry.register(BuiltInRegistries.ITEM, ResourceKey.create(Registries.ITEM, HorrorMod.id(name)), item);
	}

	public static void init() {
		CaveBlocks.init();
		CaveEntities.init();
	}
}

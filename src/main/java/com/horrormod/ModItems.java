package com.horrormod;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

public final class ModItems {
	public static final Item SPOOKY_SPIRIT_SPAWN_EGG = register("spooky_spirit_spawn_egg",
			new Item.Properties().spawnEgg(ModEntities.SPOOKY_SPIRIT));

	private ModItems() {
	}

	private static Item register(String name, Item.Properties props) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, HorrorMod.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, new SpawnEggItem(props.setId(key)));
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> output.insertAfter(Items.VEX_SPAWN_EGG, SPOOKY_SPIRIT_SPAWN_EGG));
	}
}

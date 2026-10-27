package com.horrormod;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import com.horrormod.castle.CastleItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
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

	public static final Item SUPER_PHANTOM_SPAWN_EGG = register("super_phantom_spawn_egg",
			new Item.Properties().spawnEgg(ModEntities.SUPER_PHANTOM));

	public static final Item MYSTERIOUS_WITCH_SPAWN_EGG = register("mysterious_witch_spawn_egg",
			new Item.Properties().spawnEgg(ModEntities.MYSTERIOUS_WITCH));

	/** Dropped by the Ghost when it is defeated: a whitish gray wisp with a little purple sparkle. */
	public static final Item SPIRIT = registerPlain("spirit", new Item.Properties());

	/** 0.6.0: the Horror Mod creative tab, with everything in it (Castle Level 6 first). */
	public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, HorrorMod.id("horror_mod"));
	public static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_KEY, FabricCreativeModeTab.builder()
			.title(Component.translatable("itemGroup.horrormod.horror_mod"))
			.icon(() -> new ItemStack(CastleItems.CASTLE_LEVEL_SIX))
			.displayItems((params, output) -> {
				output.accept(CastleItems.CASTLE_LEVEL_SIX);
				output.accept(CastleItems.ZIP_LINE_HOOK);
				output.accept(CastleItems.LEVEL_SEVEN_KEY);
				output.accept(CastleItems.LEVEL_SEVEN_GATE);
				output.accept(CastleItems.STRAY_BLAST_PAD);
				output.accept(CastleItems.CASTLE_LEVEL_SEVEN);
				output.accept(CastleItems.GRAPPLING_HOOK);
				output.accept(CastleItems.ELECTRA_CORE);
				output.accept(CastleItems.GRAPPLE_TARGET);
				output.accept(CastleItems.LEVEL_SEVEN_START);
				output.accept(CastleItems.GO_UP_PAD);
				output.accept(CastleItems.CASTLE_LEVEL_NINE);
				output.accept(CastleItems.LEVEL_TEN_KEY);
				output.accept(SPIRIT);
				output.accept(SPOOKY_SPIRIT_SPAWN_EGG);
				output.accept(SUPER_PHANTOM_SPAWN_EGG);
				output.accept(MYSTERIOUS_WITCH_SPAWN_EGG);
			})
			.build());

	private ModItems() {
	}

	private static Item register(String name, Item.Properties props) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, HorrorMod.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, new SpawnEggItem(props.setId(key)));
	}

	private static Item registerPlain(String name, Item.Properties props) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, HorrorMod.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, new Item(props.setId(key)));
	}

	public static void init() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> output.insertAfter(Items.PHANTOM_MEMBRANE, SPIRIT));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> output.insertAfter(Items.VEX_SPAWN_EGG, SPOOKY_SPIRIT_SPAWN_EGG));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> output.insertAfter(Items.PHANTOM_SPAWN_EGG, SUPER_PHANTOM_SPAWN_EGG));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> output.insertAfter(Items.WITCH_SPAWN_EGG, MYSTERIOUS_WITCH_SPAWN_EGG));
	}
}

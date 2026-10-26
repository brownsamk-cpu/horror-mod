package com.horrormod.castle;

import java.util.function.Function;

import com.horrormod.HorrorMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

public final class CastleItems {
	/** Use it and Castle Level 6 appears right in front of you. */
	public static final Item CASTLE_LEVEL_SIX = register("castle_level_six", CastleLevelSixItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	/** Right-click near the zip line rope (the chains) to zoom across. */
	public static final Item ZIP_LINE_HOOK = register("zip_line_hook", ZipLineHookItem::new, new Item.Properties().stacksTo(1));
	/** Opens the Level 7 Gate. */
	public static final Item LEVEL_SEVEN_KEY = register("level_seven_key", Item::new, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
	public static final Item LEVEL_SEVEN_GATE = blockItem(CastleBlocks.LEVEL_SEVEN_GATE);
	public static final Item STRAY_BLAST_PAD = blockItem(CastleBlocks.STRAY_BLAST_PAD);

	/** 0.7.0: use it and Castle Level 7 appears right in front of you (the gap goes down into the ground). */
	public static final Item CASTLE_LEVEL_SEVEN = register("castle_level_seven", CastleLevelSevenItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
	/** Look at a block up to 20 away and right-click: it pulls you there. */
	public static final Item GRAPPLING_HOOK = register("grappling_hook", GrapplingHookItem::new, new Item.Properties().stacksTo(1));
	public static final Item ELECTRA_CORE = blockItem(CastleBlocks.ELECTRA_CORE);
	public static final Item GRAPPLE_TARGET = blockItem(CastleBlocks.GRAPPLE_TARGET);
	public static final Item LEVEL_SEVEN_START = blockItem(CastleBlocks.LEVEL_SEVEN_START);
	public static final Item GO_UP_PAD = blockItem(CastleBlocks.GO_UP_PAD);

	private CastleItems() {
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, HorrorMod.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key)));
	}

	private static Item blockItem(Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, BuiltInRegistries.BLOCK.getKey(block));
		return Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(key)));
	}

	public static void init() {
		CastleBlocks.init();
		CastleRides.init();
	}
}

package com.horrormod.cave;

import com.horrormod.HorrorMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class CaveBlocks {
	/** The Stone Door: unbreakable, opens only for the Stone Key. */
	public static final Block STONE_DOOR;

	static {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, HorrorMod.id("stone_door"));
		STONE_DOOR = Registry.register(BuiltInRegistries.BLOCK, key, new StoneDoorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
				.strength(-1.0F, 3600000.0F).sound(SoundType.STONE).noLootTable().setId(key)));
	}

	private CaveBlocks() {
	}

	public static void init() {
	}
}

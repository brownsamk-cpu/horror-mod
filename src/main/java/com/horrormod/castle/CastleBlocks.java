package com.horrormod.castle;

import java.util.function.Function;

import com.horrormod.HorrorMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class CastleBlocks {
	public static final Block LEVEL_SEVEN_GATE = block("level_seven_gate", LevelSevenGateBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noLootTable());
	public static final Block STRAY_BLAST_PAD = block("stray_blast_pad", StrayBlastPadBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(1.5F).sound(SoundType.STONE).lightLevel(s -> 7));

	/** 0.7.0 Level 7: glowing white moving platform. */
	public static final Block ELECTRA_CORE = block("electra_core", ElectraCoreBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(-1.0F, 3600000.0F).sound(SoundType.AMETHYST).lightLevel(s -> 15).noLootTable());
	public static final Block GRAPPLE_TARGET = block("grapple_target", Block::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(-1.0F, 3600000.0F).sound(SoundType.METAL).lightLevel(s -> 6).noLootTable());
	public static final Block LEVEL_SEVEN_START = block("level_seven_start", Block::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(-1.0F, 3600000.0F).sound(SoundType.STONE).lightLevel(s -> 10).noLootTable());
	public static final Block RESCUE_FIELD = block("rescue_field", RescueFieldBlock::new,
			BlockBehaviour.Properties.of().noCollision().noOcclusion().strength(-1.0F, 3600000.0F).noLootTable());

	/** 0.7.1: in Level 6's last room; step on it and you whoosh up to Level 7's start pad (Level 7 is stacked on top). */
	public static final Block GO_UP_PAD = block("go_up_pad", GoUpPadBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(-1.0F, 3600000.0F).sound(SoundType.STONE).lightLevel(s -> 10).noLootTable());

	/** 0.8.0 Level 9: hidden buttons, progress lamps, and the bookshelf-looking secret door. */
	public static final Block HIDDEN_BUTTON = block("hidden_button", HiddenButtonBlock::new,
			BlockBehaviour.Properties.of().noCollision().strength(-1.0F, 3600000.0F).sound(SoundType.WOOD).noLootTable());
	public static final Block PUZZLE_LAMP = block("puzzle_lamp", PuzzleLampBlock::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_YELLOW).strength(-1.0F, 3600000.0F).sound(SoundType.GLASS)
					.lightLevel(s -> s.getValue(PuzzleLampBlock.LIT) ? 15 : 0).noLootTable());
	public static final Block SECRET_DOOR = block("secret_door", Block::new,
			BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(-1.0F, 3600000.0F).sound(SoundType.WOOD).noLootTable());

	private CastleBlocks() {
	}

	private static Block block(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
		ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, HorrorMod.id(name));
		return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(props.setId(key)));
	}

	public static void init() {
	}
}

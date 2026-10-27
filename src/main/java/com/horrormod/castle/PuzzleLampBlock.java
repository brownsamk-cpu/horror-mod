package com.horrormod.castle;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** 0.8.0 Level 9: lamp number 1..5 over the secret door; it lights when that many buttons are found. */
public class PuzzleLampBlock extends Block {
	public static final BooleanProperty LIT = BooleanProperty.create("lit");
	public static final IntegerProperty NUMBER = IntegerProperty.create("number", 1, 5);

	public PuzzleLampBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(NUMBER, 1));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT, NUMBER);
	}
}

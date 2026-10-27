package com.horrormod.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** 0.8.0 Level 9: a hidden button. The first time you press it, it counts as FOUND ("Button 2 of 5 found!") and a lamp lights. */
public class HiddenButtonBlock extends ButtonBlock {
	public static final BooleanProperty FOUND = BooleanProperty.create("found");

	public HiddenButtonBlock(Properties properties) {
		super(BlockSetType.OAK, 30, properties);
		registerDefaultState(defaultBlockState().setValue(FOUND, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FOUND);
	}

	@Override
	public void press(BlockState state, Level level, BlockPos pos, Player player) {
		super.press(state, level, pos, player);
		if (level instanceof ServerLevel server) {
			LevelNinePuzzle.pressed(server, pos, player);
		}
	}
}

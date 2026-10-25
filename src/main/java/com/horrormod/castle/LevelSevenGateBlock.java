package com.horrormod.castle;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The locked gate to level 7. Right-click it with the Level 7 Key and the whole gate swings away (you keep the key). */
public class LevelSevenGateBlock extends Block {
	public LevelSevenGateBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!stack.is(CastleItems.LEVEL_SEVEN_KEY)) {
			if (!level.isClientSide()) {
				player.sendOverlayMessage(Component.translatable("block.horrormod.level_seven_gate.locked"));
			}
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (level instanceof ServerLevel server) {
			open(server, pos);
			player.sendOverlayMessage(Component.translatable("block.horrormod.level_seven_gate.opened"));
		}
		return InteractionResult.SUCCESS;
	}

	/** Opens every gate block touching this one. Returns how many opened. */
	public static int open(ServerLevel level, BlockPos start) {
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<BlockPos> todo = new ArrayDeque<>();
		todo.add(start);
		int opened = 0;
		while (!todo.isEmpty() && opened < 64) {
			BlockPos p = todo.poll();
			if (!seen.add(p) || !level.getBlockState(p).is(CastleBlocks.LEVEL_SEVEN_GATE)) {
				continue;
			}
			level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			level.sendParticles(ParticleTypes.WITCH, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.0);
			opened++;
			for (Direction d : Direction.values()) {
				todo.add(p.relative(d));
			}
		}
		level.playSound(null, start, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 0.7F);
		return opened;
	}
}

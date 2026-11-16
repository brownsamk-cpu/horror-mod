package com.horrormod.cave;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
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

/** The Stone Door. Right-click it holding the Stone Key and the whole door rumbles away (you keep the key). */
public class StoneDoorBlock extends Block {
	public StoneDoorBlock(final Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player,
			final InteractionHand hand, final BlockHitResult hit) {
		if (!stack.is(CaveItems.STONE_KEY)) {
			if (!level.isClientSide()) {
				player.sendOverlayMessage(Component.translatable("block.horrormod.stone_door.locked"));
			}
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (level instanceof ServerLevel server) {
			open(server, pos);
			player.sendOverlayMessage(Component.translatable("block.horrormod.stone_door.opened"));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hit) {
		if (!level.isClientSide()) {
			player.sendOverlayMessage(Component.translatable("block.horrormod.stone_door.locked"));
		}
		return InteractionResult.SUCCESS;
	}

	/** Opens every Stone Door block touching this one. Returns how many opened. */
	public static int open(final ServerLevel level, final BlockPos start) {
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<BlockPos> todo = new ArrayDeque<>();
		todo.add(start);
		int opened = 0;
		while (!todo.isEmpty() && opened < 64) {
			BlockPos p = todo.poll();
			if (!seen.add(p) || !level.getBlockState(p).is(CaveBlocks.STONE_DOOR)) {
				continue;
			}
			level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
					8, 0.3, 0.3, 0.3, 0.05);
			opened++;
			for (Direction d : Direction.values()) {
				todo.add(p.relative(d));
			}
		}
		level.playSound(null, start, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 1.0F, 0.5F);
		level.playSound(null, start, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.0F, 0.6F);
		return opened;
	}
}

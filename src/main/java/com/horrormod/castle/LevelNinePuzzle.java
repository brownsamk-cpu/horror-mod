package com.horrormod.castle;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** 0.8.0 Level 9 puzzle logic: counts the found hidden buttons in the room, lights lamps, opens the secret door at 5. */
public final class LevelNinePuzzle {
	private static final int R = 16;
	private static final int UP = 9;

	public record Progress(int found, int total, boolean opened) {
	}

	private LevelNinePuzzle() {
	}

	public static Progress pressed(ServerLevel level, BlockPos pos, Player player) {
		BlockState state = level.getBlockState(pos);
		boolean wasNew = state.is(CastleBlocks.HIDDEN_BUTTON) && !state.getValue(HiddenButtonBlock.FOUND);
		if (wasNew) {
			level.setBlock(pos, state.setValue(HiddenButtonBlock.FOUND, true), Block.UPDATE_ALL);
		}
		int found = 0, total = 0;
		List<BlockPos> lamps = new ArrayList<>();
		List<BlockPos> doors = new ArrayList<>();
		for (BlockPos p : BlockPos.betweenClosed(pos.offset(-R, -UP, -R), pos.offset(R, UP, R))) {
			BlockState s = level.getBlockState(p);
			if (s.is(CastleBlocks.HIDDEN_BUTTON)) {
				total++;
				if (s.getValue(HiddenButtonBlock.FOUND)) {
					found++;
				}
			} else if (s.is(CastleBlocks.PUZZLE_LAMP)) {
				lamps.add(p.immutable());
			} else if (s.is(CastleBlocks.SECRET_DOOR)) {
				doors.add(p.immutable());
			}
		}
		for (BlockPos l : lamps) {
			BlockState s = level.getBlockState(l);
			level.setBlock(l, s.setValue(PuzzleLampBlock.LIT, s.getValue(PuzzleLampBlock.NUMBER) <= found), Block.UPDATE_ALL);
		}
		boolean opened = false;
		if (wasNew) {
			player.sendOverlayMessage(Component.translatable("block.horrormod.hidden_button.found", found, total));
			player.sendSystemMessage(Component.translatable("block.horrormod.hidden_button.found", found, total));
			level.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1.0F, 0.8F + 0.15F * found);
			level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.0);
			if (found >= total && total > 0) {
				for (BlockPos d : doors) {
					level.setBlock(d, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
					level.sendParticles(ParticleTypes.WITCH, d.getX() + 0.5, d.getY() + 0.5, d.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.0);
				}
				opened = !doors.isEmpty();
				level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.0F);
				player.sendSystemMessage(Component.translatable("block.horrormod.hidden_button.all"));
				player.sendOverlayMessage(Component.translatable("block.horrormod.hidden_button.all"));
			}
		} else {
			player.sendOverlayMessage(Component.translatable("block.horrormod.hidden_button.again", found, total));
		}
		return new Progress(found, total, opened);
	}
}

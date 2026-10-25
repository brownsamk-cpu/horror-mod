package com.horrormod.castle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;

/**
 * Electra Core (June's idea for Level 7): a glowing WHITE square with a faint electric sparkle.
 * Touching cores make one platform. The platform slides one block at a time in its FACING direction,
 * and when it bumps into something it turns around. Whoever stands on top rides along. It never hurts.
 * Only the "leader" (the lowest block of the platform) keeps a scheduled tick, so every platform moves once per step.
 */
public class ElectraCoreBlock extends HorizontalDirectionalBlock {
	/** Ticks between steps (one block per step). */
	public static final int STEP_TICKS = 7;
	private static boolean moving;

	public ElectraCoreBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean movedByPiston) {
		if (!moving && !level.isClientSide() && !old.is(this)) {
			level.scheduleTick(pos, this, STEP_TICKS);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(4) == 0) {
			level.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.getX() + random.nextDouble(), pos.getY() + 1.02, pos.getZ() + random.nextDouble(), 0, 0.02, 0);
		}
	}

	/** All touching Electra Cores (the whole platform). */
	public static List<BlockPos> platform(Level level, BlockPos start) {
		List<BlockPos> out = new ArrayList<>();
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<BlockPos> todo = new ArrayDeque<>();
		todo.add(start);
		while (!todo.isEmpty() && out.size() < 64) {
			BlockPos p = todo.poll();
			if (!seen.add(p) || !level.getBlockState(p).is(CastleBlocks.ELECTRA_CORE)) {
				continue;
			}
			out.add(p);
			for (Direction d : Direction.values()) {
				todo.add(p.relative(d));
			}
		}
		return out;
	}

	private static BlockPos leader(List<BlockPos> group) {
		BlockPos best = group.get(0);
		for (BlockPos p : group) {
			if (p.compareTo(best) < 0) {
				best = p;
			}
		}
		return best;
	}

	private static boolean canMove(Level level, List<BlockPos> group, Direction dir) {
		Set<BlockPos> set = new HashSet<>(group);
		for (BlockPos p : group) {
			BlockPos n = p.relative(dir);
			if (!set.contains(n) && !level.getBlockState(n).isAir()) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		List<BlockPos> group = platform(level, pos);
		if (group.isEmpty()) {
			return;
		}
		BlockPos lead = leader(group);
		if (!lead.equals(pos)) {
			// someone else leads this platform; make sure they keep it going
			if (!level.getBlockTicks().hasScheduledTick(lead, this)) {
				level.scheduleTick(lead, this, STEP_TICKS);
			}
			return;
		}
		Direction dir = state.getValue(FACING);
		if (!canMove(level, group, dir)) {
			dir = dir.getOpposite();
			if (!canMove(level, group, dir)) {
				level.scheduleTick(pos, this, STEP_TICKS);
				return;
			}
		}
		step(level, group, dir);
		level.scheduleTick(lead.relative(dir), this, STEP_TICKS);
	}

	/** Moves the platform one block and carries everyone standing on it. */
	public static void step(ServerLevel level, List<BlockPos> group, Direction dir) {
		AABB top = null;
		for (BlockPos p : group) {
			AABB a = new AABB(p.getX(), p.getY() + 0.5, p.getZ(), p.getX() + 1, p.getY() + 1.6, p.getZ() + 1);
			top = top == null ? a : top.minmax(a);
		}
		List<Entity> riders = level.getEntities((Entity) null, top.inflate(0.3, 0, 0.3), e -> !e.isSpectator() && !e.noPhysics);
		moving = true;
		try {
			BlockState s = CastleBlocks.ELECTRA_CORE.defaultBlockState().setValue(FACING, dir);
			for (BlockPos p : group) {
				level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
			}
			for (BlockPos p : group) {
				level.setBlock(p.relative(dir), s, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
			}
		} finally {
			moving = false;
		}
		double dx = dir.getStepX(), dz = dir.getStepZ();
		for (Entity e : riders) {
			if (e instanceof ServerPlayer sp) {
				if (CastleRides.isRiding(sp)) {
					continue;
				}
				sp.teleportTo(e.getX() + dx, e.getY(), e.getZ() + dz);
			} else {
				e.setPos(e.getX() + dx, e.getY(), e.getZ() + dz);
			}
			e.resetFallDistance();
		}
	}
}

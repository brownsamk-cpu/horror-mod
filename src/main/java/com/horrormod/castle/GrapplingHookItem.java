package com.horrormod.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Grappling Hook: look at a block (up to 20 blocks away), right-click, and the hook grabs it and pulls you there. Use it again and again! */
public class GrapplingHookItem extends Item {
	public static final double REACH = 20.0;
	public static final double SPEED = 0.9;

	public GrapplingHookItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer sp) {
			if (!grapple(sp)) {
				sp.sendOverlayMessage(Component.translatable("item.horrormod.grappling_hook.too_far"));
				return InteractionResult.FAIL;
			}
		}
		return InteractionResult.SUCCESS;
	}

	/** Shoots the hook where the player looks. Returns true if it grabbed a block. */
	public static boolean grapple(ServerPlayer player) {
		if (CastleRides.isRiding(player)) {
			return false;
		}
		HitResult hit = player.pick(REACH, 1.0F, false);
		if (!(hit instanceof BlockHitResult bh) || hit.getType() != HitResult.Type.BLOCK) {
			return false;
		}
		ServerLevel level = (ServerLevel) player.level();
		BlockPos bp = bh.getBlockPos();
		Vec3 anchor = bh.getLocation();
		Vec3 to;
		if (level.getBlockState(bp.above()).getCollisionShape(level, bp.above()).isEmpty()
				&& level.getBlockState(bp.above(2)).getCollisionShape(level, bp.above(2)).isEmpty()) {
			to = Vec3.atBottomCenterOf(bp.above()); // land on top of it
		} else {
			Vec3 n = Vec3.atLowerCornerOf(bh.getDirection().getUnitVec3i());
			to = anchor.add(n.scale(0.6)).add(0, -1.0, 0);
		}
		int ticks = Math.max(4, (int) Math.ceil(player.position().distanceTo(to) / SPEED));
		CastleRides.start(player, new CastleRides.Ride(player.position(), to, ticks, 0.8, anchor));
		level.playSound(null, player.blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.3F);
		return true;
	}
}

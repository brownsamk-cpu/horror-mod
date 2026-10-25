package com.horrormod.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** "Castle Level 6": right-click and the course is built in front of you (the door is 2 blocks ahead). */
public class CastleLevelSixItem extends Item {
	public CastleLevelSixItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel server) {
			Direction forward = player.getDirection();
			BlockPos origin = CastleLevelSix.originInFrontOf(player.blockPosition(), forward);
			CastleLevelSix.build(server, origin, forward);
			server.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.4F, 1.6F);
			player.sendOverlayMessage(Component.translatable("item.horrormod.castle_level_six.built"));
		}
		return InteractionResult.SUCCESS;
	}
}

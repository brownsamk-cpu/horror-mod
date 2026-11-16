package com.horrormod.cave;

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

/** "Stony Cave": right-click and the cave is built in front of you, Stone Monster and all. */
public class StonyCaveItem extends Item {
	public StonyCaveItem(final Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
		if (level instanceof ServerLevel server) {
			Direction forward = player.getDirection();
			StonyCave.build(server, StonyCave.originInFrontOf(player.blockPosition(), forward), forward);
			server.playSound(null, player.blockPosition(), SoundEvents.AMBIENT_CAVE.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
			player.sendOverlayMessage(Component.translatable("item.horrormod.stony_cave.built"));
		}
		return InteractionResult.SUCCESS;
	}
}

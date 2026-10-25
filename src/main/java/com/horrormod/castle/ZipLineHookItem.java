package com.horrormod.castle;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Zip Line Hook: right-click while standing near the chain rope and you slide all the way to the other end. */
public class ZipLineHookItem extends Item {
	public ZipLineHookItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer sp) {
			if (!CastleRides.startZipLine(sp)) {
				sp.sendOverlayMessage(Component.translatable("item.horrormod.zip_line_hook.too_far"));
				return InteractionResult.FAIL;
			}
		}
		return InteractionResult.SUCCESS;
	}
}

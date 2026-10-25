package com.horrormod.client;

import com.horrormod.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class HorrorModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(SpookySpiritModel.LAYER, SpookySpiritModel::createBodyLayer);
		EntityRendererRegistry.register(ModEntities.SPOOKY_SPIRIT, SpookySpiritRenderer::new);
	}
}

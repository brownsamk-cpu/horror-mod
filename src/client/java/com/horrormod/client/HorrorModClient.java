package com.horrormod.client;

import com.horrormod.HorrorMod;
import com.horrormod.ModEntities;
import com.horrormod.ModParticles;
import com.horrormod.MysteriousEffects;
import com.horrormod.MysteriousPotions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class HorrorModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(SpookySpiritModel.LAYER, SpookySpiritModel::createBodyLayer);
		EntityRendererRegistry.register(ModEntities.SPOOKY_SPIRIT, SpookySpiritRenderer::new);
		ModelLayerRegistry.registerModelLayer(SuperPhantomModel.LAYER, SuperPhantomModel::createBodyLayer);
		EntityRendererRegistry.register(ModEntities.SUPER_PHANTOM, SuperPhantomRenderer::new);
		ModelLayerRegistry.registerModelLayer(MysteriousWitchModel.LAYER, MysteriousWitchModel::createBodyLayer);
		EntityRendererRegistry.register(ModEntities.MYSTERIOUS_WITCH, MysteriousWitchRenderer::new);
		ParticleProviderRegistry.getInstance().register(ModParticles.BAT, BatParticle.Provider::new);
		// Bat Potion: a light spooky purple tint over the screen
		HudElementRegistry.addFirst(HorrorMod.id("bat_tint"), (graphics, delta) -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null && mc.player.hasEffect(MysteriousPotions.BAT)) {
				graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0x38401060);
			}
		});
		// Slime Feet Potion: your own player slides like on ice (player movement happens on the client)
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (mc.player != null && mc.player.hasEffect(MysteriousPotions.SLIME_FEET)) {
				MysteriousEffects.SlimeFeet.slide(mc.player);
			}
		});
	}
}

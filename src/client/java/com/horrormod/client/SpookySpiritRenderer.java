package com.horrormod.client;

import com.horrormod.HorrorMod;
import com.horrormod.SpookySpirit;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class SpookySpiritRenderer extends MobRenderer<SpookySpirit, LivingEntityRenderState, SpookySpiritModel> {
	private static final Identifier TEXTURE = HorrorMod.id("textures/entity/spooky_spirit.png");

	public SpookySpiritRenderer(final EntityRendererProvider.Context context) {
		super(context, new SpookySpiritModel(context.bakeLayer(SpookySpiritModel.LAYER)), 0.2F);
	}

	@Override
	public Identifier getTextureLocation(final LivingEntityRenderState state) {
		return TEXTURE;
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}
}

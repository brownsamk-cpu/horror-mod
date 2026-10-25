package com.horrormod.client;

import com.horrormod.HorrorMod;
import com.horrormod.MysteriousWitch;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class MysteriousWitchRenderer extends MobRenderer<MysteriousWitch, MysteriousWitchRenderState, MysteriousWitchModel> {
	private static final Identifier TEXTURE = HorrorMod.id("textures/entity/mysterious_witch.png");
	private static final RenderType EYES = RenderTypes.eyes(HorrorMod.id("textures/entity/mysterious_witch_eyes.png"));

	public MysteriousWitchRenderer(final EntityRendererProvider.Context context) {
		super(context, new MysteriousWitchModel(context.bakeLayer(MysteriousWitchModel.LAYER)), 0.5F);
		this.addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return EYES;
			}
		});
	}

	@Override
	public Identifier getTextureLocation(final MysteriousWitchRenderState state) {
		return TEXTURE;
	}

	@Override
	public MysteriousWitchRenderState createRenderState() {
		return new MysteriousWitchRenderState();
	}

	@Override
	public void extractRenderState(final MysteriousWitch entity, final MysteriousWitchRenderState state, final float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.angry = entity.isAngry();
	}
}

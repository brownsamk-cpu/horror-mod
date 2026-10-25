package com.horrormod.client;

import com.horrormod.HorrorMod;
import com.horrormod.SuperPhantom;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class SuperPhantomRenderer extends MobRenderer<SuperPhantom, LivingEntityRenderState, SuperPhantomModel> {
	private static final Identifier TEXTURE = HorrorMod.id("textures/entity/super_phantom.png");
	private static final RenderType EYES = RenderTypes.eyes(HorrorMod.id("textures/entity/super_phantom_eyes.png"));
	public static final float SCALE = 1.8F; // much bigger than a vanilla phantom

	public SuperPhantomRenderer(final EntityRendererProvider.Context context) {
		super(context, new SuperPhantomModel(context.bakeLayer(SuperPhantomModel.LAYER)), 1.2F);
		this.addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return EYES;
			}
		});
	}

	@Override
	public Identifier getTextureLocation(final LivingEntityRenderState state) {
		return TEXTURE;
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	@Override
	protected void scale(final LivingEntityRenderState state, final PoseStack poseStack) {
		poseStack.scale(SCALE, SCALE, SCALE);
		poseStack.translate(0.0F, 1.3125F, 0.1875F);
	}

	@Override
	protected void setupRotations(final LivingEntityRenderState state, final PoseStack poseStack, final float bodyRot, final float entityScale) {
		super.setupRotations(state, poseStack, bodyRot, entityScale);
		poseStack.rotateDegrees(Axis.XP, state.xRot);
	}
}

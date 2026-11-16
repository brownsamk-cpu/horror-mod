package com.horrormod.client;

import com.horrormod.HorrorMod;
import com.horrormod.cave.StoneMonster;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** Stone Monster renderer: stone skin plus glowing red eyes (they shine even in the dark cave). */
public class StoneMonsterRenderer extends MobRenderer<StoneMonster, StoneMonsterRenderState, StoneMonsterModel> {
	private static final Identifier TEXTURE = HorrorMod.id("textures/entity/stone_monster.png");
	private static final RenderType EYES = RenderTypes.eyes(HorrorMod.id("textures/entity/stone_monster_eyes.png"));

	public StoneMonsterRenderer(final EntityRendererProvider.Context context) {
		super(context, new StoneMonsterModel(context.bakeLayer(StoneMonsterModel.LAYER)), 1.4F);
		this.addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return EYES;
			}
		});
	}

	@Override
	public Identifier getTextureLocation(final StoneMonsterRenderState state) {
		return TEXTURE;
	}

	@Override
	public StoneMonsterRenderState createRenderState() {
		return new StoneMonsterRenderState();
	}

	@Override
	public void extractRenderState(final StoneMonster entity, final StoneMonsterRenderState state, final float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.throwProgress = entity.getThrowProgress(partialTicks);
		state.slamProgress = entity.getSlamProgress(partialTicks);
	}
}

package com.horrormod.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class StoneMonsterRenderState extends LivingEntityRenderState {
	/** 0..1 through the throw (0 = not throwing). */
	public float throwProgress;
	/** 0..1 through the ground slam (0 = not slamming). */
	public float slamProgress;
}

package com.horrormod.client;

import com.horrormod.HorrorMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/** Spooky Spirit: a big round-ish head, a sheet body that gets narrower toward a wavy tail, and two little arms.
 *  Drawn with a TRANSLUCENT render type so the see-through texture lets you see blocks behind it.
 *  Box layout matches tools/spooky_spirit.py, which paints the texture. */
public class SpookySpiritModel extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(HorrorMod.id("spooky_spirit"), "main");
	private final ModelPart ghost;
	private final ModelPart head;
	private final ModelPart tail;
	private final ModelPart leftArm;
	private final ModelPart rightArm;

	public SpookySpiritModel(final ModelPart root) {
		super(root, RenderTypes::entityTranslucent);
		this.ghost = root.getChild("ghost");
		this.head = this.ghost.getChild("head");
		this.tail = this.ghost.getChild("tail");
		this.leftArm = this.ghost.getChild("left_arm");
		this.rightArm = this.ghost.getChild("right_arm");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition ghost = root.addOrReplaceChild("ghost", CubeListBuilder.create(), PartPose.offset(0.0F, 10.0F, 0.0F));
		ghost.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
		ghost.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-3.5F, 0.0F, -3.0F, 7.0F, 6.0F, 6.0F), PartPose.ZERO);
		ghost.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 32).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 4.0F, 4.0F),
				PartPose.offset(0.0F, 6.0F, 0.5F));
		ghost.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 16).addBox(-2.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
				PartPose.offsetAndRotation(-3.5F, 1.0F, 0.0F, 0.0F, 0.0F, 0.35F));
		ghost.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 16).mirror().addBox(0.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F),
				PartPose.offsetAndRotation(3.5F, 1.0F, 0.0F, 0.0F, 0.0F, -0.35F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(final LivingEntityRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
		// gentle hover bob and a wavy tail
		this.ghost.y += Mth.sin(t * 0.1F) * 1.2F;
		this.tail.xRot = 0.25F + Mth.sin(t * 0.15F) * 0.15F;
		this.tail.zRot = Mth.sin(t * 0.11F) * 0.12F;
		// little arms wave slowly, like "wooo"
		this.leftArm.zRot = 0.35F + Mth.sin(t * 0.12F) * 0.25F;
		this.rightArm.zRot = -0.35F - Mth.sin(t * 0.12F + 1.0F) * 0.25F;
	}
}

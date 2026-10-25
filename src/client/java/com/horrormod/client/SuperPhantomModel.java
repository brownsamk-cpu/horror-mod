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
import net.minecraft.util.Mth;

/** Super Phantom: a phantom-shaped flyer (body, head, two-part wings, two-part tail) with slow, big wing flaps.
 *  Box layout matches tools/super_phantom.py, which paints the texture; the renderer scales it up. */
public class SuperPhantomModel extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(HorrorMod.id("super_phantom"), "main");
	private final ModelPart leftWingBase, leftWingTip, rightWingBase, rightWingTip, tailBase, tailTip;

	public SuperPhantomModel(final ModelPart root) {
		super(root);
		ModelPart body = root.getChild("body");
		this.tailBase = body.getChild("tail_base");
		this.tailTip = this.tailBase.getChild("tail_tip");
		this.leftWingBase = body.getChild("left_wing_base");
		this.leftWingTip = this.leftWingBase.getChild("left_wing_tip");
		this.rightWingBase = body.getChild("right_wing_base");
		this.rightWingTip = this.rightWingBase.getChild("right_wing_tip");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 8).addBox(-3.0F, -2.0F, -8.0F, 5.0F, 3.0F, 9.0F),
				PartPose.rotation(-0.1F, 0.0F, 0.0F));
		PartDefinition tailBase = body.addOrReplaceChild("tail_base", CubeListBuilder.create().texOffs(3, 20).addBox(-2.0F, 0.0F, 0.0F, 3.0F, 2.0F, 6.0F),
				PartPose.offset(0.0F, -2.0F, 1.0F));
		tailBase.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(4, 29).addBox(-1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 6.0F), PartPose.offset(0.0F, 0.5F, 6.0F));
		PartDefinition lw = body.addOrReplaceChild("left_wing_base", CubeListBuilder.create().texOffs(23, 12).addBox(0.0F, 0.0F, 0.0F, 6.0F, 2.0F, 9.0F),
				PartPose.offsetAndRotation(2.0F, -2.0F, -8.0F, 0.0F, 0.0F, 0.1F));
		lw.addOrReplaceChild("left_wing_tip", CubeListBuilder.create().texOffs(16, 24).addBox(0.0F, 0.0F, 0.0F, 13.0F, 1.0F, 9.0F),
				PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1F));
		PartDefinition rw = body.addOrReplaceChild("right_wing_base", CubeListBuilder.create().texOffs(23, 12).mirror().addBox(-6.0F, 0.0F, 0.0F, 6.0F, 2.0F, 9.0F),
				PartPose.offsetAndRotation(-3.0F, -2.0F, -8.0F, 0.0F, 0.0F, -0.1F));
		rw.addOrReplaceChild("right_wing_tip", CubeListBuilder.create().texOffs(16, 24).mirror().addBox(-13.0F, 0.0F, 0.0F, 13.0F, 1.0F, 9.0F),
				PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1F));
		body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -2.0F, -5.0F, 7.0F, 3.0F, 5.0F),
				PartPose.offsetAndRotation(0.0F, 1.0F, -7.0F, 0.2F, 0.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(final LivingEntityRenderState state) {
		super.setupAnim(state);
		float anim = state.ageInTicks * 0.09F; // slower, bigger flaps than a vanilla phantom
		float flap = Mth.cos(anim) * 22.0F * Mth.DEG_TO_RAD;
		this.leftWingBase.zRot = flap;
		this.leftWingTip.zRot = flap;
		this.rightWingBase.zRot = -flap;
		this.rightWingTip.zRot = -flap;
		this.tailBase.xRot = -(5.0F + Mth.cos(anim * 2.0F) * 5.0F) * Mth.DEG_TO_RAD;
		this.tailTip.xRot = this.tailBase.xRot;
	}
}

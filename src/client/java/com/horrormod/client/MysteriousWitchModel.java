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
import net.minecraft.util.Mth;

/** Mysterious Witch: minty head with a crooked nose, a tall CROOKED starry hat (brim, middle, bent tip, bent tip-tip),
 *  a long robe and skirt, and two arms. Box layout matches tools/mysterious_witch.py (texture 128x64). */
public class MysteriousWitchModel extends EntityModel<MysteriousWitchRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(HorrorMod.id("mysterious_witch"), "main");
	private final ModelPart head, hatTip, rightArm, leftArm, skirt;

	public MysteriousWitchModel(final ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.hatTip = this.head.getChild("brim").getChild("hat_mid").getChild("hat_tip");
		this.rightArm = root.getChild("right_arm");
		this.leftArm = root.getChild("left_arm");
		this.skirt = root.getChild("skirt");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
		head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, -4.0F, -6.0F, 2.0F, 3.0F, 2.0F),
				PartPose.rotation(0.15F, 0.0F, 0.0F));
		PartDefinition brim = head.addOrReplaceChild("brim", CubeListBuilder.create().texOffs(64, 0).addBox(-6.0F, -1.0F, -6.0F, 12.0F, 1.0F, 12.0F),
				PartPose.offsetAndRotation(0.0F, -7.5F, 0.0F, -0.05F, 0.0F, 0.05F));
		PartDefinition mid = brim.addOrReplaceChild("hat_mid", CubeListBuilder.create().texOffs(64, 16).addBox(-3.5F, -4.0F, -3.5F, 7.0F, 4.0F, 7.0F),
				PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.1F, 0.0F, 0.05F));
		PartDefinition tip = mid.addOrReplaceChild("hat_tip", CubeListBuilder.create().texOffs(64, 32).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F),
				PartPose.offsetAndRotation(0.5F, -4.0F, 0.5F, -0.2F, 0.0F, 0.3F));
		tip.addOrReplaceChild("hat_tip2", CubeListBuilder.create().texOffs(96, 32).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -0.1F, 0.0F, 0.6F));
		root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 13.0F, 6.0F), PartPose.ZERO);
		root.addOrReplaceChild("skirt", CubeListBuilder.create().texOffs(0, 36).addBox(-5.0F, 0.0F, -4.0F, 10.0F, 11.0F, 8.0F), PartPose.offset(0.0F, 13.0F, 0.0F));
		root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(32, 16).addBox(-3.0F, -1.0F, -1.5F, 3.0F, 10.0F, 3.0F),
				PartPose.offset(-4.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 16).mirror().addBox(0.0F, -1.0F, -1.5F, 3.0F, 10.0F, 3.0F),
				PartPose.offset(4.0F, 2.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 64);
	}

	@Override
	public void setupAnim(final MysteriousWitchRenderState state) {
		super.setupAnim(state);
		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
		float walk = state.walkAnimationPos, speed = state.walkAnimationSpeed, t = state.ageInTicks;
		this.rightArm.xRot = Mth.cos(walk * 0.6662F + Mth.PI) * 1.2F * speed;
		this.leftArm.xRot = Mth.cos(walk * 0.6662F) * 1.2F * speed;
		this.rightArm.zRot = 0.1F;
		this.leftArm.zRot = -0.1F;
		if (state.angry) {
			// right arm raised high, ready to throw a potion; left arm waves
			this.rightArm.xRot = -2.6F + Mth.sin(t * 0.3F) * 0.2F;
			this.leftArm.xRot = -0.6F + Mth.sin(t * 0.25F) * 0.3F;
		}
		this.skirt.xRot = speed * 0.15F;
		this.hatTip.zRot = 0.3F + Mth.sin(t * 0.08F) * 0.08F; // the crooked tip wobbles a little
	}
}

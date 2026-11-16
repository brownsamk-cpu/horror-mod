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

/**
 * Stone Monster: a 5-block-tall rocky giant (80 px). Big blocky head with a heavy brow and glowing red eyes, a wide
 * boulder body with rocks on the shoulders, long arms with huge fists, and thick legs. Box layout matches
 * tools/stone_monster.py (texture 128x128).
 */
public class StoneMonsterModel extends EntityModel<StoneMonsterRenderState> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(HorrorMod.id("stone_monster"), "main");
	private final ModelPart head, body, rightArm, leftArm, rightLeg, leftLeg;

	public StoneMonsterModel(final ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.head = this.body.getChild("head");
		this.rightArm = this.body.getChild("right_arm");
		this.leftArm = this.body.getChild("left_arm");
		this.rightLeg = root.getChild("right_leg");
		this.leftLeg = root.getChild("left_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		// body pivots at its bottom (y = -6) so it can lean forward for the slam
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 32).addBox(-12.0F, -34.0F, -7.0F, 24.0F, 34.0F, 14.0F),
				PartPose.offset(0.0F, -6.0F, 0.0F));
		body.addOrReplaceChild("right_rock", CubeListBuilder.create().texOffs(64, 8).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F),
				PartPose.offsetAndRotation(-9.0F, -33.0F, 1.0F, 0.1F, 0.3F, -0.15F));
		body.addOrReplaceChild("left_rock", CubeListBuilder.create().texOffs(64, 8).mirror().addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F),
				PartPose.offsetAndRotation(9.0F, -33.0F, 2.0F, -0.1F, -0.2F, 0.2F));
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F),
				PartPose.offset(0.0F, -34.0F, -2.0F));
		head.addOrReplaceChild("brow", CubeListBuilder.create().texOffs(64, 0).addBox(-9.0F, -2.0F, -2.0F, 18.0F, 4.0F, 4.0F),
				PartPose.offset(0.0F, -13.0F, -7.5F));
		PartDefinition rightArm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 80).addBox(-5.0F, -3.0F, -5.0F, 10.0F, 36.0F, 10.0F),
				PartPose.offset(-17.0F, -30.0F, 0.0F));
		rightArm.addOrReplaceChild("right_fist", CubeListBuilder.create().texOffs(40, 80).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 10.0F, 12.0F),
				PartPose.offset(0.0F, 30.0F, 0.0F));
		PartDefinition leftArm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 80).mirror().addBox(-5.0F, -3.0F, -5.0F, 10.0F, 36.0F, 10.0F),
				PartPose.offset(17.0F, -30.0F, 0.0F));
		leftArm.addOrReplaceChild("left_fist", CubeListBuilder.create().texOffs(40, 80).mirror().addBox(-6.0F, 0.0F, -6.0F, 12.0F, 10.0F, 12.0F),
				PartPose.offset(0.0F, 30.0F, 0.0F));
		root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(76, 32).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 30.0F, 10.0F),
				PartPose.offset(-6.5F, -6.0F, 0.0F));
		root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(76, 32).mirror().addBox(-5.0F, 0.0F, -5.0F, 10.0F, 30.0F, 10.0F),
				PartPose.offset(6.5F, -6.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(final StoneMonsterRenderState state) {
		super.setupAnim(state);
		float walk = state.walkAnimationPos, speed = Math.min(state.walkAnimationSpeed, 1.0F), t = state.ageInTicks;
		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
		// heavy, slow stomping walk
		this.rightLeg.xRot = Mth.cos(walk * 0.4F) * 0.7F * speed;
		this.leftLeg.xRot = Mth.cos(walk * 0.4F + Mth.PI) * 0.7F * speed;
		float breathe = Mth.sin(t * 0.05F) * 0.04F;
		this.rightArm.xRot = Mth.cos(walk * 0.4F + Mth.PI) * 0.5F * speed + breathe;
		this.leftArm.xRot = Mth.cos(walk * 0.4F) * 0.5F * speed - breathe;
		this.rightArm.zRot = 0.08F;
		this.leftArm.zRot = -0.08F;
		this.body.xRot = 0.0F;
		float th = state.throwProgress;
		if (th > 0.0F) {
			// wind-up: right arm swings far back over the head, then whips forward to throw the rock
			if (th < 0.5F) {
				float k = th / 0.5F;
				this.rightArm.xRot = Mth.lerp(k, 0.0F, -3.0F);
				this.body.xRot = Mth.lerp(k, 0.0F, -0.12F);
			} else {
				float k = (th - 0.5F) / 0.5F;
				this.rightArm.xRot = Mth.lerp(Math.min(1.0F, k * 2.0F), -3.0F, -0.6F);
				this.body.xRot = Mth.lerp(Math.min(1.0F, k * 2.0F), -0.12F, 0.15F) * (1.0F - k * 0.5F);
			}
		}
		float sl = state.slamProgress;
		if (sl > 0.0F) {
			// both fists lift high... then SLAM down into the ground, leaning forward
			float arms, lean;
			if (sl < 0.5F) {
				float k = sl / 0.5F;
				arms = Mth.lerp(k, 0.0F, -2.9F);
				lean = Mth.lerp(k, 0.0F, -0.15F);
			} else {
				float k = Math.min(1.0F, (sl - 0.5F) / 0.15F);
				arms = Mth.lerp(k, -2.9F, -0.9F);
				lean = Mth.lerp(k, -0.15F, 0.45F) * (sl > 0.8F ? (1.0F - (sl - 0.8F) / 0.2F) : 1.0F);
			}
			this.rightArm.xRot = arms;
			this.leftArm.xRot = arms;
			this.body.xRot = lean;
		}
	}
}

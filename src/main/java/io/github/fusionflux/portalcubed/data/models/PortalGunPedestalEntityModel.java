package io.github.fusionflux.portalcubed.data.models;
// Made with Blockbench 4.10.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class PortalGunPedestalEntityModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("portalcubed", "portal_gun_pedestal"), "main");
	private final ModelPart platform;
	private final ModelPart lower_piston;
	private final ModelPart upper_piston;
	private final ModelPart pivot;
	private final ModelPart locking_bars;
	private final ModelPart south_hatch;
	private final ModelPart s_segment_1;
	private final ModelPart s_segment_2;
	private final ModelPart s_segment_3;
	private final ModelPart north_hatch;
	private final ModelPart n_segment_1;
	private final ModelPart n_segment_2;
	private final ModelPart n_segment_3;

	public PortalGunPedestalEntityModel(ModelPart root) {
		this.platform = root.getChild("platform");
		this.lower_piston = root.getChild("lower_piston");
		this.upper_piston = root.getChild("upper_piston");
		this.pivot = root.getChild("pivot");
		this.locking_bars = root.getChild("locking_bars");
		this.south_hatch = root.getChild("south_hatch");
		this.s_segment_1 = root.getChild("s_segment_1");
		this.s_segment_2 = root.getChild("s_segment_2");
		this.s_segment_3 = root.getChild("s_segment_3");
		this.north_hatch = root.getChild("north_hatch");
		this.n_segment_1 = root.getChild("n_segment_1");
		this.n_segment_2 = root.getChild("n_segment_2");
		this.n_segment_3 = root.getChild("n_segment_3");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition platform = partdefinition.addOrReplaceChild("platform", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -1.0F, -4.5F, 9.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition cube_r1 = platform.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(20, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition lower_piston = platform.addOrReplaceChild("lower_piston", CubeListBuilder.create(), PartPose.offset(0.0F, -6.5F, 0.0F));

		PartDefinition cube_r2 = lower_piston.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 15).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition upper_piston = lower_piston.addOrReplaceChild("upper_piston", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, 0.0F));

		PartDefinition cube_r3 = upper_piston.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(9, 15).addBox(-0.5F, -10.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.5F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition pivot = upper_piston.addOrReplaceChild("pivot", CubeListBuilder.create().texOffs(42, -8).addBox(0.0F, 0.0F, -4.5F, 0.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.5F, 0.0F));

		PartDefinition cube_r4 = pivot.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(42, -1).addBox(10.025F, -2.0F, -3.5F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(42, -1).addBox(9.975F, -2.0F, -3.5F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(42, -1).addBox(1.025F, -2.0F, -3.5F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(42, -1).addBox(0.975F, -2.0F, -3.5F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 5.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r5 = pivot.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(29, 4).addBox(-2.5F, -1.725F, 0.25F, 5.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 1.5F, 0.0F, 0.0F, -1.5708F, 0.3927F));

		PartDefinition cube_r6 = pivot.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(29, 1).addBox(-2.5F, -1.5F, 0.0F, 5.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.668F, 1.1869F, 0.0F, 0.0F, 1.5708F, 0.3927F));

		PartDefinition cube_r7 = pivot.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(20, 22).addBox(-1.5F, -18.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(14, 15).addBox(-1.0F, -15.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 18.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition locking_bars = pivot.addOrReplaceChild("locking_bars", CubeListBuilder.create(), PartPose.offset(-0.1F, -2.5F, 0.0F));

		PartDefinition cube_r8 = locking_bars.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(42, 4).addBox(1.0F, -3.5F, -3.5F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(42, 4).addBox(10.0F, -3.5F, -3.5F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0872F, -0.0019F, 5.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition south_hatch = partdefinition.addOrReplaceChild("south_hatch", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition s_segment_1 = south_hatch.addOrReplaceChild("s_segment_1", CubeListBuilder.create().texOffs(32, 20).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.75F, 6.0F));

		PartDefinition s_segment_2 = south_hatch.addOrReplaceChild("s_segment_2", CubeListBuilder.create().texOffs(32, 18).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.75F, 6.0F));

		PartDefinition s_segment_3 = south_hatch.addOrReplaceChild("s_segment_3", CubeListBuilder.create().texOffs(32, 16).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.75F, 6.0F));

		PartDefinition north_hatch = partdefinition.addOrReplaceChild("north_hatch", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition n_segment_1 = north_hatch.addOrReplaceChild("n_segment_1", CubeListBuilder.create().texOffs(32, 22).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.75F, -6.0F));

		PartDefinition n_segment_2 = north_hatch.addOrReplaceChild("n_segment_2", CubeListBuilder.create().texOffs(32, 24).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.75F, -6.0F));

		PartDefinition n_segment_3 = north_hatch.addOrReplaceChild("n_segment_3", CubeListBuilder.create().texOffs(32, 26).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.75F, -6.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		platform.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		south_hatch.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		north_hatch.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}

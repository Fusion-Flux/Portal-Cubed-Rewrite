package io.github.fusionflux.portalcubed.content.boots;

import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class LongFallBootsModel extends HumanoidArmorModel<HumanoidRenderState> {
	private static final float DEFORMATION = 1.0f; // Vanilla value for boots not stored in a constant anywhere.
	private static final float LEG_DEFORMATION = DEFORMATION - 0.1f; // 0.1 is the vanilla offset also not stored in a constant anywhere.
	private static final float INNER_LEG_DEFORMATION = LEG_DEFORMATION - 0.05f;
	private static final float PRONG_OFFSET = (4f * LEG_DEFORMATION) + 0.2f;

	public static final LongFallBootsModel INSTANCE = new LongFallBootsModel(createMesh().getRoot().bake(64, 32));

	public LongFallBootsModel(ModelPart root) {
		super(root);
	}

	public static MeshDefinition createMesh() {
		MeshDefinition meshDefinition = HumanoidModel.createMesh(new CubeDeformation(DEFORMATION), 0f);
		PartDefinition partDefinition = meshDefinition.getRoot();
		CubeDeformation legDeformation = new CubeDeformation(LEG_DEFORMATION);
		CubeDeformation innerLegDeformation = new CubeDeformation(INNER_LEG_DEFORMATION);
		CubeDeformation prongDeformation = new CubeDeformation(0, LEG_DEFORMATION, LEG_DEFORMATION);
		partDefinition.addOrReplaceChild(
				"right_leg",
				CubeListBuilder.create()
						.texOffs(0, 16).addBox(-2f, 0f, -2f, 4f, 12f, 4f, legDeformation)
						.texOffs(48, 0).addBox(-2f, 0f, -2f, 4f, 12f, 4f, innerLegDeformation)
						.texOffs(34, -3).addBox(0f, 3f, PRONG_OFFSET, 0f, 9f, 3f, prongDeformation),
				PartPose.offset(-1.9f, 12f, 0f)
		);
		partDefinition.addOrReplaceChild(
				"left_leg",
				CubeListBuilder.create()
						.texOffs(0, 16).mirror().addBox(-2f, 0f, -2f, 4f, 12f, 4f, legDeformation)
						.texOffs(48, 0).mirror().addBox(-2f, 0f, -2f, 4f, 12f, 4f, innerLegDeformation)
						.texOffs(34, -3).mirror().addBox(0f, 3f, PRONG_OFFSET, 0f, 9f, 3f, prongDeformation),
				PartPose.offset(1.9f, 12f, 0f)
		);
		return meshDefinition;
	}
}

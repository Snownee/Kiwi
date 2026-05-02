package snownee.kiwi.contributor.impl.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class PlanetModel<T extends HumanoidRenderState> extends HumanoidModel<T> {

	private final ModelPart largePlanet;
	private final ModelPart smallPlanet;

	public PlanetModel(ModelPart root) {
		super(root);
		this.largePlanet = root.getChild("planet_large");
		this.smallPlanet = root.getChild("planet_small");
	}

	public static LayerDefinition create() {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition root = meshdefinition.getRoot();
		root.clearChild("body");
		PartDefinition head = root.clearChild("head");
		head.clearChild("hat");
		root.clearChild("left_arm");
		root.clearChild("right_arm");
		root.clearChild("left_leg");
		root.clearChild("right_leg");
		root.addOrReplaceChild(
				"planet_large", CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-2.0F, 0, -2.0F, 4.0F, 4.0F, 4.0F)
						.texOffs(-9, 8)
						.addBox(-4.5F, 2f, -4.5F, 9.0F, 0.0F, 9.0F),
				PartPose.offset(0.0F, -6.0F, -20.0F));
		root.addOrReplaceChild(
				"planet_small",
				CubeListBuilder.create().texOffs(16, 0).addBox(-1.5F, 1f, -1.5F, 3.0F, 3.0F, 3.0F),
				PartPose.offset(0.0F, -6.0F, 16.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(T renderState) {
		super.setupAnim(renderState);
		largePlanet.yRot = -renderState.ageInTicks / 10;
		smallPlanet.yRot = -renderState.ageInTicks / 6;
	}

}
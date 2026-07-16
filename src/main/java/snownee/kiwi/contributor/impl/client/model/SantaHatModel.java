package snownee.kiwi.contributor.impl.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

public class SantaHatModel<T extends HumanoidRenderState> extends HumanoidModel<T> {

	private PlayerModel playerModel;
	private ModelPart main;

	public SantaHatModel(PlayerModel playerModel, ModelPart root) {
		super(root);
		this.playerModel = playerModel;
		main = root.getChild("head").getChild("santa");
	}

	public static LayerDefinition create() {
		MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, false);
		PartDefinition root = mesh.getRoot().clearRecursively();
		PartDefinition head = root.getChild("head");
		head.clearChild("hat");
		CubeListBuilder builder = CubeListBuilder.create();
		builder.texOffs(0, 8).addBox(-0.5F, -10.0F, -3.0F, 5.0F, 2.0F, 5.0F);
		builder.texOffs(0, 0).addBox(0.0F, -12.0F, -2.5F, 4.0F, 3.0F, 4.0F);
		builder.texOffs(16, 4).addBox(4.0F, -12.5F, -1.5F, 2.0F, 4.0F, 2.0F);
		builder.texOffs(12, 0).addBox(5.0F, -9.0F, -1.0F, 2.0F, 2.0F, 2.0F);
		head.addOrReplaceChild("santa", builder, PartPose.ZERO);
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(T renderState) {
		super.setupAnim(renderState);
	}

}
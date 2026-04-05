package snownee.kiwi.contributor.impl.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class SantaHatModel extends Model<AvatarRenderState> {

	private final PlayerModel playerModel;
	private final ModelPart main;

	public SantaHatModel(PlayerModel playerModel, LayerDefinition definition) {
		super(definition.bakeRoot(), RenderTypes::entitySolid);
		this.playerModel = playerModel;
		main = this.root().getChild("main");
	}

	public static LayerDefinition create() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition root = meshdefinition.getRoot();
		CubeListBuilder builder = CubeListBuilder.create();
		builder.texOffs(0, 8).addBox(-0.5F, -10.0F, -3.0F, 5.0F, 2.0F, 5.0F);
		builder.texOffs(0, 0).addBox(0.0F, -12.0F, -2.5F, 4.0F, 3.0F, 4.0F);
		builder.texOffs(16, 4).addBox(4.0F, -12.5F, -1.5F, 2.0F, 4.0F, 2.0F);
		builder.texOffs(12, 0).addBox(5.0F, -9.0F, -1.0F, 2.0F, 2.0F, 2.0F);
		root.addOrReplaceChild("main", builder, PartPose.ZERO);
		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(AvatarRenderState state) {
		main.copyFrom(playerModel.head);
	}

}

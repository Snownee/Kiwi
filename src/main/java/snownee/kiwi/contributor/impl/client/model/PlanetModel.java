package snownee.kiwi.contributor.impl.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class PlanetModel extends Model<AvatarRenderState> {

	private final ModelPart largePlanet;
	private final ModelPart smallPlanet;

	public PlanetModel(LayerDefinition definition) {
		super(definition.bakeRoot(), RenderTypes::entityTranslucent);
		this.largePlanet = this.root().getChild("large");
		this.smallPlanet = this.root().getChild("small");
	}

	public static LayerDefinition create() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition root = meshdefinition.getRoot();
		root.addOrReplaceChild("large", CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-2.0F, -36.0F, -2.0F, 4.0F, 4.0F, 4.0F)
				.texOffs(-9, 8)
				.addBox(-4.5F, -34.0F, -4.5F, 9.0F, 0.0F, 9.0F), PartPose.offset(0.0F, 20.0F, -20.0F));
		root.addOrReplaceChild(
				"small",
				CubeListBuilder.create().texOffs(16, 0).addBox(-1.5F, -35.0F, -1.5F, 3.0F, 3.0F, 3.0F),
				PartPose.offset(0.0F, 20.0F, 16.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(AvatarRenderState state) {
		largePlanet.yRot = -state.ageInTicks / 10;
		smallPlanet.yRot = -state.ageInTicks / 6;
	}

}
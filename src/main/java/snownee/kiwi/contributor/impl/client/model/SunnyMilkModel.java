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
import net.minecraft.util.Mth;

public class SunnyMilkModel extends Model<AvatarRenderState> {

	private final ModelPart wingRight;
	private final ModelPart wingLeft;

	public SunnyMilkModel(LayerDefinition definition) {
		super(definition.bakeRoot(), RenderTypes::entityTranslucent);
		wingLeft = this.root().getChild("wingLeft");
		wingRight = this.root().getChild("wingRight");
	}

	public static LayerDefinition create() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition root = meshdefinition.getRoot();

		CubeListBuilder wingLeft = CubeListBuilder.create();
		wingLeft.texOffs(0, 12).addBox(0.5F, -5.5F, 0.0F, 0.0F, 12.0F, 20.0F);
		wingLeft.texOffs(0, 28).addBox(0.5F, 6.5F, 0.0F, 0.0F, 10.0F, 16.0F);

		CubeListBuilder wingRight = CubeListBuilder.create();
		wingRight.texOffs(0, 12).addBox(-0.5F, -5.5F, 0.0F, 0.0F, 12.0F, 20.0F);
		wingRight.texOffs(0, 28).addBox(-0.5F, 6.5F, 0.0F, 0.0F, 10.0F, 16.0F);

		root.addOrReplaceChild("wingLeft", wingLeft, PartPose.offset(-1.9641F, -2.0F, 2.0F));
		root.addOrReplaceChild("wingRight", wingRight, PartPose.offset(1.9641F, -2.0F, 2.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(AvatarRenderState state) {
		float ticks = state.ageInTicks * 0.5f;
		wingLeft.yRot = -1.0472F + Mth.sin(ticks) * 0.25f;
		wingRight.yRot = -wingLeft.yRot;
	}

}

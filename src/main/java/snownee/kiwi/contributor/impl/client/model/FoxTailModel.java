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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;

public class FoxTailModel extends Model<AvatarRenderState> {

	private final PlayerModel playerModel;
	private final ModelPart tail;
	private final ModelPart ear1;
	private final ModelPart ear2;

	public FoxTailModel(PlayerModel playerModel, LayerDefinition definition) {
		super(definition.bakeRoot(), RenderTypes::entitySolid);
		this.playerModel = playerModel;
		this.ear1 = this.root().getChild("right_ear");
		this.ear2 = this.root().getChild("left_ear");
		this.tail = this.root().getChild("tail");
	}

	public static LayerDefinition create() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition root = meshdefinition.getRoot();

		root.addOrReplaceChild(
				"right_ear",
				CubeListBuilder.create().texOffs(8, 1).addBox(-4.0F, -10.0F, -4.0F, 2.0F, 2.0F, 1.0F),
				PartPose.ZERO);
		root.addOrReplaceChild(
				"left_ear",
				CubeListBuilder.create().texOffs(15, 1).addBox(2.0F, -10.0F, -4.0F, 2.0F, 2.0F, 1.0F),
				PartPose.ZERO);
		root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(30, 0).addBox(0F, 0F, 0F, 4.0F, 9.0F, 5.0F), PartPose.ZERO);

		return LayerDefinition.create(meshdefinition, 48, 32);
	}

	@Override
	public void setupAnim(AvatarRenderState state) {
		ear1.loadPose(playerModel.head.storePose());
		ear2.loadPose(playerModel.head.storePose());
		float ageInTicks = state.ageInTicks;
		if (ageInTicks % 60 < 2) {
			ear1.yRot += 0.05f;
			ear2.yRot -= 0.05f;
		}
		float delta = Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		if (state.hasPose(Pose.CROUCHING)) {
			this.tail.setPos(-2.0F, 14.0F, 5.5F);
			this.tail.xRot = 1.25F + delta;
		} else {
			this.tail.setPos(-2.0F, 10.0F, .5F);
			this.tail.xRot = 0.85F + delta;
		}
	}

}

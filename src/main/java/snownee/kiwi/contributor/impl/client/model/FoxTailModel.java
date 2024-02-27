package snownee.kiwi.contributor.impl.client.model;

import com.google.common.collect.ImmutableList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class FoxTailModel<T extends LivingEntity> extends AgeableListModel<T> {

	private PlayerModel<AbstractClientPlayer> playerModel;
	private ModelPart tail;
	private ModelPart ear1;
	private ModelPart ear2;

	public FoxTailModel(PlayerModel<AbstractClientPlayer> playerModel, LayerDefinition definition) {
		this.playerModel = playerModel;

		ModelPart root = definition.bakeRoot();
		this.ear1 = root.getChild("right_ear");
		this.ear2 = root.getChild("left_ear");
		this.tail = root.getChild("tail");
	}

	public static LayerDefinition create() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition root = meshdefinition.getRoot();

		//		this.ear1 = new ModelPart(this, 8, 1);
		//		this.ear1.addBox(-4.0F, -10.0F, -4.0F, 2.0F, 2.0F, 1.0F);
		//		this.ear2 = new ModelPart(this, 15, 1);
		//		this.ear2.addBox(2.0F, -10.0F, -4.0F, 2.0F, 2.0F, 1.0F);
		//		this.tail = new ModelPart(this, 30, 0);
		//		this.tail.addBox(0F, 0F, 0F, 4.0F, 9.0F, 5.0F);

		//PartDefinition partdefinition1 = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(1, 5).addBox(-3.0F, -2.0F, -5.0F, 8.0F, 6.0F, 6.0F), PartPose.offset(-1.0F, 16.5F, -3.0F));
		root.addOrReplaceChild(
				"right_ear",
				CubeListBuilder.create().texOffs(8, 1).addBox(-4.0F, -10.0F, -4.0F, 2.0F, 2.0F, 1.0F),
				PartPose.ZERO);
		root.addOrReplaceChild(
				"left_ear",
				CubeListBuilder.create().texOffs(15, 1).addBox(2.0F, -10.0F, -4.0F, 2.0F, 2.0F, 1.0F),
				PartPose.ZERO);

		//PartDefinition partdefinition2 = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(24, 15).addBox(-3.0F, 3.999F, -3.5F, 6.0F, 11.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 16.0F, -6.0F, ((float) Math.PI / 2F), 0.0F, 0.0F));
		root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(30, 0).addBox(0F, 0F, 0F, 4.0F, 9.0F, 5.0F), PartPose.ZERO);

		return LayerDefinition.create(meshdefinition, 48, 32);
	}

	@Override
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of(ear1, ear2);
	}

	@Override
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(tail);
	}

	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		ear1.copyFrom(playerModel.head);
		ear2.copyFrom(playerModel.head);
		if (ageInTicks % 60 < 2) {
			ear1.yRot += 0.05f;
			ear2.yRot -= 0.05f;
		}
		float delta = Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		if (entityIn.isCrouching()) {
			this.tail.setPos(-2.0F, 14.0F, 5.5F);
			this.tail.xRot = 1.25F + delta;
		} else {
			this.tail.setPos(-2.0F, 10.0F, .5F);
			this.tail.xRot = 0.85F + delta;
		}

	}

}

package snownee.kiwi.contributor.impl.client.layer;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Items;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.impl.client.model.SunnyMilkModel;

public class SunnyMilkLayer extends CosmeticLayer {
	private static final Identifier TEXTURE = Kiwi.id("textures/reward/sunny_milk.png");
	private static final Supplier<LayerDefinition> definition = Suppliers.memoize(SunnyMilkModel::create);
	private final SunnyMilkModel model;

	public SunnyMilkLayer(RenderLayerParent<AvatarRenderState, PlayerModel> entityRendererIn) {
		super(entityRendererIn);
		model = new SunnyMilkModel(definition.get());
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
		if (state.isInvisible || state.hasPose(Pose.SLEEPING)) {
			return;
		}
		if (state.chestEquipment.is(Items.ELYTRA)) {
			return;
		}
		poseStack.pushPose();
		model.setupAnim(state);
		renderer.getModel().body.translateAndRotate(poseStack);
		submitNodeCollector.submitModel(model, state, poseStack, RenderTypes.entityTranslucent(TEXTURE), lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
		poseStack.popPose();
	}

}

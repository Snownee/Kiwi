package snownee.kiwi.contributor.impl.client.layer;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.impl.client.model.SunnyMilkModel;

public class SunnyMilkLayer extends CosmeticLayer {
	private static final Identifier TEXTURE = Kiwi.id("textures/reward/sunny_milk.png");
	private static final Supplier<LayerDefinition> definition = Suppliers.memoize(SunnyMilkModel::create);
	private final SunnyMilkModel<AvatarRenderState> model;

	public SunnyMilkLayer(RenderLayerParent<AvatarRenderState, PlayerModel> entityRendererIn) {
		super(entityRendererIn);
		model = new SunnyMilkModel<>(definition.get().bakeRoot());
	}

	@Override
	public void submit(
			PoseStack matrixStackIn,
			SubmitNodeCollector submitNodeCollector,
			int lightCoords,
			AvatarRenderState renderState,
			float yRot,
			float xRot) {
		if (renderState.pose == Pose.SLEEPING) {
			return;
		}
		if (renderState.showCape && renderState.chestEquipment.get(DataComponents.GLIDER) != null) {
			return;
		}
		matrixStackIn.pushPose();
//		model.young = renderState.isBaby;
		model.setupAnim(renderState);
		VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderTypes.entityTranslucent(TEXTURE), false, false);
		getParentModel().body.translateAndRotate(matrixStackIn);
		model.renderToBuffer(matrixStackIn, ivertexbuilder, lightCoords, OverlayTexture.NO_OVERLAY);
		matrixStackIn.popPose();
	}
}

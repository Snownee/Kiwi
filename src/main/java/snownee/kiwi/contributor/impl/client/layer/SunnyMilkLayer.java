package snownee.kiwi.contributor.impl.client.layer;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.impl.client.model.SunnyMilkModel;

public class SunnyMilkLayer extends CosmeticLayer {
	private static final ResourceLocation TEXTURE = Kiwi.id("textures/reward/sunny_milk.png");
	private static final Supplier<LayerDefinition> definition = Suppliers.memoize(SunnyMilkModel::create);
	private final SunnyMilkModel<PlayerRenderState> model;

	public SunnyMilkLayer(RenderLayerParent<PlayerRenderState, PlayerModel> entityRendererIn) {
		super(entityRendererIn);
		model = new SunnyMilkModel<>(definition.get().bakeRoot());
	}

	@Override
	public void render(
			PoseStack matrixStackIn,
			MultiBufferSource bufferIn,
			int packedLightIn,
			PlayerRenderState renderState,
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
		VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entityTranslucent(TEXTURE), false, false);
		getParentModel().body.translateAndRotate(matrixStackIn);
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY);
		matrixStackIn.popPose();
	}

}

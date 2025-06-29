package snownee.kiwi.contributor.impl.client.layer;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.impl.client.model.SantaHatModel;

public class SantaHatLayer extends CosmeticLayer {
	private static final ResourceLocation TEXTURE = Kiwi.id("textures/reward/santa.png");
	private static final Supplier<LayerDefinition> definition = Suppliers.memoize(SantaHatModel::create);
	private final SantaHatModel<PlayerRenderState> modelSantaHat;
	private final SantaHatModel<PlayerRenderState> modelBabySantaHat;

	public SantaHatLayer(RenderLayerParent<PlayerRenderState, PlayerModel> entityRendererIn) {
		super(entityRendererIn);
		modelSantaHat = new SantaHatModel<>(entityRendererIn.getModel(), definition.get().bakeRoot());
		modelBabySantaHat = new SantaHatModel<>(entityRendererIn.getModel(), definition.get().apply(HumanoidModel.BABY_TRANSFORMER).bakeRoot());
	}

	@Override
	public void render(
			PoseStack matrixStackIn,
			MultiBufferSource bufferIn,
			int packedLightIn,
			PlayerRenderState renderState,
			float yRot,
			float xRot) {
		if (!renderState.headEquipment.isEmpty()) {
			return;
		}
		matrixStackIn.pushPose();
		SantaHatModel<PlayerRenderState> model = renderState.isBaby ? modelBabySantaHat : modelSantaHat;
		model.setupAnim(renderState);
		VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entitySolid(TEXTURE), false, false);
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY);
		matrixStackIn.popPose();
	}

}

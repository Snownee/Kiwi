package snownee.kiwi.contributor.impl.client.layer;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

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
import snownee.kiwi.contributor.impl.client.model.PlanetModel;

public class PlanetLayer extends CosmeticLayer {
	private static final ResourceLocation TEXTURE = Kiwi.id("textures/reward/planet.png");
	private static final Supplier<LayerDefinition> definition = Suppliers.memoize(PlanetModel::create);
	private final PlanetModel<PlayerRenderState> modelPlanet;

	public PlanetLayer(RenderLayerParent<PlayerRenderState, PlayerModel> entityRendererIn) {
		super(entityRendererIn);
		modelPlanet = new PlanetModel<>(definition.get().bakeRoot());
	}

	@Override
	public void render(
			PoseStack matrixStackIn,
			MultiBufferSource bufferIn,
			int packedLightIn,
			PlayerRenderState renderState,
			float yRot,
			float xRot) {
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(Axis.YP.rotationDegrees(-renderState.ageInTicks));
		float scale = 0.7f;
		matrixStackIn.scale(scale, scale, scale);
		modelPlanet.setupAnim(renderState);
		VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entityTranslucent(TEXTURE), false, false);
		modelPlanet.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY);
		matrixStackIn.popPose();
	}

}

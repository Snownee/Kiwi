package snownee.kiwi.contributor.impl.client.layer;

import java.util.Locale;

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
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.impl.client.model.FoxTailModel;

public class FoxTailLayer extends CosmeticLayer {
	private static final ResourceLocation FOX = ResourceLocation.withDefaultNamespace("textures/entity/fox/fox.png");
	private static final ResourceLocation SNOW_FOX = ResourceLocation.withDefaultNamespace("textures/entity/fox/snow_fox.png");
	private static final Supplier<LayerDefinition> definition = Suppliers.memoize(FoxTailModel::create);
	private final FoxTailModel<PlayerRenderState> modelFoxTail;

	public FoxTailLayer(RenderLayerParent<PlayerRenderState, PlayerModel> entityRendererIn) {
		super(entityRendererIn);
		modelFoxTail = new FoxTailModel<>(entityRendererIn.getModel(), definition.get().bakeRoot());
	}

	@Override
	public void render(
			PoseStack matrixStackIn,
			MultiBufferSource bufferIn,
			int packedLightIn,
			PlayerRenderState renderState,
			float yRot,
			float xRot) {
		if (renderState.showCape && renderState.chestEquipment.get(DataComponents.GLIDER) != null) {
			return;
		}
		String name = renderState.name.toLowerCase(Locale.ENGLISH);
		ResourceLocation texture = name.contains("snow") || name.contains("xue") || name.contains("yuki") ? SNOW_FOX : FOX;
		matrixStackIn.pushPose();
		modelFoxTail.setupAnim(renderState);
		VertexConsumer vertexConsumer = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entitySolid(texture), false, false);
		modelFoxTail.renderToBuffer(matrixStackIn, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY);
		matrixStackIn.popPose();
	}

}

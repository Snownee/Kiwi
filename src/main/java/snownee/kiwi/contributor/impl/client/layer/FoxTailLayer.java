package snownee.kiwi.contributor.impl.client.layer;

import java.util.Locale;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.impl.client.model.FoxTailModel;

public class FoxTailLayer extends CosmeticLayer {
	private static final ResourceLocation FOX = ResourceLocation.withDefaultNamespace("textures/entity/fox/fox.png");
	private static final ResourceLocation SNOW_FOX = ResourceLocation.withDefaultNamespace("textures/entity/fox/snow_fox.png");
	private static final Supplier<LayerDefinition> definition = Suppliers.memoize(FoxTailModel::create);
	private final FoxTailModel<AbstractClientPlayer> modelFoxTail;

	public FoxTailLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> entityRendererIn) {
		super(entityRendererIn);
		modelFoxTail = new FoxTailModel<>(entityRendererIn.getModel(), definition.get());
	}

	@Override
	public void render(
			PoseStack matrixStackIn,
			MultiBufferSource bufferIn,
			int packedLightIn,
			AbstractClientPlayer entitylivingbaseIn,
			float limbSwing,
			float limbSwingAmount,
			float partialTicks,
			float ageInTicks,
			float netHeadYaw,
			float headPitch) {
		if (entitylivingbaseIn.isInvisible() || entitylivingbaseIn.isSleeping()) {
			return;
		}
		ItemStack itemstack = entitylivingbaseIn.getItemBySlot(EquipmentSlot.CHEST);
		if (itemstack.getItem() instanceof ElytraItem) {
			return;
		}
		String name = entitylivingbaseIn.getName().getString().toLowerCase(Locale.ENGLISH);
		ResourceLocation texture = name.contains("snow") || name.contains("xue") || name.contains("yuki") ? SNOW_FOX : FOX;
		matrixStackIn.pushPose();
		modelFoxTail.young = entitylivingbaseIn.isBaby();
		modelFoxTail.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entitySolid(texture), false, false);
		modelFoxTail.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY);
		matrixStackIn.popPose();
	}

}

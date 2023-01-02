package snownee.kiwi.contributor.impl.client.layer;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.impl.client.model.SunnyMilkModel;

@OnlyIn(Dist.CLIENT)
public class SunnyMilkLayer extends CosmeticLayer {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Kiwi.ID, "textures/reward/sunny_milk.png");
	private static final LazyOptional<LayerDefinition> definition = LazyOptional.of(SunnyMilkModel::create);
	private final SunnyMilkModel<AbstractClientPlayer> model;

	public SunnyMilkLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> entityRendererIn) {
		super(entityRendererIn);
		model = new SunnyMilkModel<>(definition.orElse(null));
	}

	@Override
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entitylivingbaseIn.isInvisible() || entitylivingbaseIn.isSleeping()) {
			return;
		}
		ItemStack itemstack = entitylivingbaseIn.getItemBySlot(EquipmentSlot.CHEST);
		if (itemstack.getItem() instanceof ElytraItem) {
			return;
		}
		matrixStackIn.pushPose();
		model.young = entitylivingbaseIn.isBaby();
		model.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entityTranslucent(TEXTURE), false, false);
		renderer.getModel().body.translateAndRotate(matrixStackIn);
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.popPose();
	}

}

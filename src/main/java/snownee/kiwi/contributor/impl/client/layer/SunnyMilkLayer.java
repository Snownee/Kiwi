package snownee.kiwi.contributor.impl.client.layer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.client.RewardLayer;
import snownee.kiwi.contributor.impl.client.model.SunnyMilkModel;

@OnlyIn(Dist.CLIENT)
public class SunnyMilkLayer extends RewardLayer {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Kiwi.MODID, "textures/reward/sunny_milk.png");
	private final SunnyMilkModel<AbstractClientPlayerEntity> model;

	public SunnyMilkLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRendererIn) {
		super(entityRendererIn);
		model = new SunnyMilkModel<>(entityRendererIn.getEntityModel());
	}

	@Override
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entitylivingbaseIn.isInvisible() || entitylivingbaseIn.isSleeping()) {
			return;
		}
		ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EquipmentSlotType.CHEST);
		if (itemstack.getItem() instanceof ElytraItem) {
			return;
		}
		matrixStackIn.push();
		model.isChild = entitylivingbaseIn.isChild();
		model.setRotationAngles(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		IVertexBuilder ivertexbuilder = ItemRenderer.getBuffer(bufferIn, RenderType.getEntityTranslucent(TEXTURE), false, false);
		entityRenderer.getEntityModel().bipedBody.translateRotate(matrixStackIn);
		model.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.pop();
	}

}

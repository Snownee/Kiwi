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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.client.RewardLayer;
import snownee.kiwi.contributor.impl.client.model.ElectronicatModel;

@OnlyIn(Dist.CLIENT)
public class ElectronicatLayer extends RewardLayer {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Kiwi.MODID, "textures/reward/electronicat.png");
	private final ElectronicatModel<AbstractClientPlayerEntity> model;
	private final ElectronicatModel.Emissive<AbstractClientPlayerEntity> emissive;

	public ElectronicatLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRendererIn) {
		super(entityRendererIn);
		model = new ElectronicatModel<>(entityRendererIn.getModel());
		emissive = new ElectronicatModel.Emissive<>(entityRendererIn.getModel());
	}

	@Override
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entitylivingbaseIn.isInvisible()) {
			return;
		}
		//        ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EquipmentSlotType.CHEST);
		//        if (itemstack.getItem() instanceof ElytraItem) {
		//            return;
		//        }
		ItemStack itemstack = entitylivingbaseIn.getItemBySlot(EquipmentSlotType.HEAD);
		if (!itemstack.isEmpty()) {
			return;
		}
		matrixStackIn.pushPose();
		model.young = entitylivingbaseIn.isBaby();
		emissive.young = entitylivingbaseIn.isBaby();
		model.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		emissive.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		renderer.getModel().head.translateAndRotate(matrixStackIn);
		IVertexBuilder ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entityCutout(TEXTURE), false, false);
		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entityCutout(TEXTURE), false, false);
		emissive.renderToBuffer(matrixStackIn, ivertexbuilder, 15728880, OverlayTexture.NO_OVERLAY, 0.75F + MathHelper.sin(ageInTicks * 0.05F) * 0.25F, 1.0F, 1.0F, 1.0F);
		matrixStackIn.popPose();
	}

}

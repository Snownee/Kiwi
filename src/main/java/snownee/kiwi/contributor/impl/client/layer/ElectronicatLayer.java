//package snownee.kiwi.contributor.impl.client.layer;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//
//import net.minecraft.client.model.PlayerModel;
//import net.minecraft.client.player.AbstractClientPlayer;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.entity.ItemRenderer;
//import net.minecraft.client.renderer.entity.RenderLayerParent;
//import net.minecraft.client.renderer.texture.OverlayTexture;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.Mth;
//import net.minecraft.world.entity.EquipmentSlot;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import snownee.kiwi.Kiwi;
//import snownee.kiwi.contributor.client.RewardLayer;
//import snownee.kiwi.contributor.impl.client.model.ElectronicatModel;
//
//@OnlyIn(Dist.CLIENT)
//public class ElectronicatLayer extends RewardLayer {
//	private static final ResourceLocation TEXTURE = new ResourceLocation(Kiwi.MODID, "textures/reward/electronicat.png");
//	private final ElectronicatModel<AbstractClientPlayer> model;
//	private final ElectronicatModel.Emissive<AbstractClientPlayer> emissive;
//
//	public ElectronicatLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> entityRendererIn) {
//		super(entityRendererIn);
//		model = new ElectronicatModel<>(entityRendererIn.getModel());
//		emissive = new ElectronicatModel.Emissive<>(entityRendererIn.getModel());
//	}
//
//	@Override
//	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
//		if (entitylivingbaseIn.isInvisible()) {
//			return;
//		}
//		//        ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EquipmentSlotType.CHEST);
//		//        if (itemstack.getItem() instanceof ElytraItem) {
//		//            return;
//		//        }
//		ItemStack itemstack = entitylivingbaseIn.getItemBySlot(EquipmentSlot.HEAD);
//		if (!itemstack.isEmpty()) {
//			return;
//		}
//		matrixStackIn.pushPose();
//		model.young = entitylivingbaseIn.isBaby();
//		emissive.young = entitylivingbaseIn.isBaby();
//		model.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
//		emissive.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
//		renderer.getModel().head.translateAndRotate(matrixStackIn);
//		VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entityCutout(TEXTURE), false, false);
//		model.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
//		ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entityCutout(TEXTURE), false, false);
//		emissive.renderToBuffer(matrixStackIn, ivertexbuilder, 15728880, OverlayTexture.NO_OVERLAY, 0.75F + Mth.sin(ageInTicks * 0.05F) * 0.25F, 1.0F, 1.0F, 1.0F);
//		matrixStackIn.popPose();
//	}
//
//}

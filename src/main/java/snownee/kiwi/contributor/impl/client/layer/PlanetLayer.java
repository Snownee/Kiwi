//package snownee.kiwi.contributor.impl.client.layer;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import com.mojang.math.Vector3f;
//
//import net.minecraft.client.model.PlayerModel;
//import net.minecraft.client.player.AbstractClientPlayer;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.entity.ItemRenderer;
//import net.minecraft.client.renderer.entity.RenderLayerParent;
//import net.minecraft.client.renderer.texture.OverlayTexture;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import snownee.kiwi.Kiwi;
//import snownee.kiwi.contributor.client.RewardLayer;
//import snownee.kiwi.contributor.impl.client.model.PlanetModel;
//
//@OnlyIn(Dist.CLIENT)
//public class PlanetLayer extends RewardLayer {
//	private static final ResourceLocation TEXTURE = new ResourceLocation(Kiwi.MODID, "textures/reward/planet.png");
//	private final PlanetModel<AbstractClientPlayer> modelPlanet;
//
//	public PlanetLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> entityRendererIn) {
//		super(entityRendererIn);
//		modelPlanet = new PlanetModel<>();
//	}
//
//	@Override
//	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
//		if (entitylivingbaseIn.isInvisible()) {
//			return;
//		}
//		matrixStackIn.pushPose();
//		matrixStackIn.translate(0, -0.6, 0);
//		matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-ageInTicks));
//		matrixStackIn.scale(1.2f, 1.2f, 1.2f);
//		modelPlanet.setupAnim(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
//		VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entityTranslucent(TEXTURE), false, false);
//		modelPlanet.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
//		matrixStackIn.popPose();
//	}
//
//}

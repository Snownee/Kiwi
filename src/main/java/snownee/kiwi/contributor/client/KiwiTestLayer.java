package snownee.kiwi.contributor.client;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KiwiTestLayer extends RewardLayer {

    public KiwiTestLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(AbstractClientPlayerEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn) {
        GlStateManager.pushMatrix();
        //GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.scalef(0.035F, 0.035F, 0.035F);
        GlStateManager.disableLighting();
        Minecraft.getInstance().fontRenderer.drawStringWithShadow("dian雪尼，谢谢！", -30, -30, 0xFFFFFFFF);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        //GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

}

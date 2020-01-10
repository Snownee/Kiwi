package snownee.kiwi.contributor.client;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
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
    public void func_225628_a_(MatrixStack matrix, IRenderTypeBuffer buffer, int p_225628_3_, AbstractClientPlayerEntity entityIn, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        //        GlStateManager.pushMatrix();
        //        GlStateManager.enableBlend();
        //        GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
        //        GlStateManager.scalef(0.035F, 0.035F, 0.035F);
        //        GlStateManager.disableLighting();
        //        Minecraft.getInstance().fontRenderer.drawStringWithShadow("dian雪尼，谢谢！", -30, -30, 0xFFFFFFFF);
        //        GlStateManager.disableBlend();
        //        GlStateManager.enableLighting();
        //        GlStateManager.popMatrix();
    }

}

package snownee.kiwi.client;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class RenderUtil
{
    public static void beginWorld()
    {
        Minecraft mc = Minecraft.getInstance();
        Vec3d view = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.color4f(1, 1, 1, 1);
        GlStateManager.translated(-view.x, -view.y, -view.z);
    }

    public static void endWorld()
    {
        GlStateManager.disableAlphaTest();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawRepeatedModalRect(int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, boolean flipX, boolean flipY)
    {
        for (int i = 0; i < width; i += uWidth)
        {
            int w = Math.min(uWidth, width - i);
            for (int j = 0; j < height; j += vHeight)
            {
                int h = Math.min(vHeight, height - j);
                drawFlippedModalRect(x + i, y + j, w, h, u, v, flipX, flipY, 256, 256);
            }
        }
    }

    public static void drawFlippedModalRect(int x, int y, int width, int height, int u, int v, boolean flipX, boolean flipY, float textureWidth, float textureHeight)
    {
        float scaleX = 1F / textureWidth;
        float scaleY = 1F / textureHeight;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        float u1 = u * scaleX;
        float u2 = (u + width) * scaleX;
        float v1 = v * scaleY;
        float v2 = (v + height) * scaleY;

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0.0D).tex(flipX ? u2 : u1, flipY ? v1 : v2).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(flipX ? u1 : u2, flipY ? v1 : v2).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(flipX ? u1 : u2, flipY ? v2 : v1).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(flipX ? u2 : u1, flipY ? v2 : v1).endVertex();
        tessellator.draw();
    }
}

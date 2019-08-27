/*
The MIT License (MIT)

Copyright (c) 2014-2015 mezz

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package snownee.kiwi.client.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class ResourceDrawable implements IStaticDrawable
{
    private final ResourceLocation resourceLocation;
    private final int textureWidth;
    private final int textureHeight;

    private final int u;
    private final int v;
    private final int width;
    private final int height;
    private final int paddingTop;
    private final int paddingBottom;
    private final int paddingLeft;
    private final int paddingRight;

    public ResourceDrawable(ResourceLocation resourceLocation, int u, int v, int width, int height)
    {
        this(resourceLocation, u, v, width, height, 0, 0, 0, 0, 256, 256);
    }

    public ResourceDrawable(ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight, int textureWidth, int textureHeight)
    {
        this.resourceLocation = resourceLocation;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;

        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;

        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
    }

    @Override
    public int getWidth()
    {
        return width + paddingLeft + paddingRight;
    }

    @Override
    public int getHeight()
    {
        return height + paddingTop + paddingBottom;
    }

    @Override
    public void draw(int xOffset, int yOffset)
    {
        draw(xOffset, yOffset, 0, 0, 0, 0);
    }

    @Override
    public void draw(int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(this.resourceLocation);

        int x = xOffset + this.paddingLeft + maskLeft;
        int y = yOffset + this.paddingTop + maskTop;
        int u = this.u + maskLeft;
        int v = this.v + maskTop;
        int width = this.width - maskRight - maskLeft;
        int height = this.height - maskBottom - maskTop;
        float f = 1.0F / this.textureWidth;
        float f1 = 1.0F / this.textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0.0D).tex(u * f, (v + (float) height) * f1).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex((u + (float) width) * f, (v + (float) height) * f1).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex((u + (float) width) * f, v * f1).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }
}

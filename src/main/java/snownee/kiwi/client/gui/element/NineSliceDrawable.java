////package snownee.kiwi.client.gui.element;
////
////import net.minecraft.client.Minecraft;
////import net.minecraft.util.ResourceLocation;
////
////public class DrawableNineSlice implements IDrawable
////{
////    private final IDrawableStatic leftTop;
////    private final IDrawableStatic leftMiddle;
////    private final IDrawableStatic leftBottom;
////    private final IDrawableStatic middleTop;
////    private final IDrawableStatic middleMiddle;
////    private final IDrawableStatic middleBottom;
////    private final IDrawableStatic rightTop;
////    private final IDrawableStatic rightMiddle;
////    private final IDrawableStatic rightBottom;
////    private int width;
////    private int height;
////
////    public DrawableNineSlice(ResourceLocation resourceLocation, int u, int v, int width, int height, int leftWidth, int rightWidth, int topHeight, int bottomHeight)
////    {
////        final int uMiddle = u + leftWidth;
////        final int uRight = u + width - rightWidth;
////        final int vMiddle = v + topHeight;
////        final int vBottom = v + height - bottomHeight;
////
////        final int middleWidth = uRight - uMiddle;
////        final int middleHeight = vBottom - vMiddle;
////
////        this.leftTop = new DrawableResource(resourceLocation, u, v, leftWidth, topHeight);
////        this.leftMiddle = new DrawableResource(resourceLocation, u, vMiddle, leftWidth, middleHeight);
////        this.leftBottom = new DrawableResource(resourceLocation, u, vBottom, leftWidth, bottomHeight);
////        this.middleTop = new DrawableResource(resourceLocation, uMiddle, v, middleWidth, topHeight);
////        this.middleMiddle = new DrawableResource(resourceLocation, uMiddle, vMiddle, middleWidth, middleHeight);
////        this.middleBottom = new DrawableResource(resourceLocation, uMiddle, vBottom, middleWidth, bottomHeight);
////        this.rightTop = new DrawableResource(resourceLocation, uRight, v, rightWidth, topHeight);
////        this.rightMiddle = new DrawableResource(resourceLocation, uRight, vMiddle, rightWidth, middleHeight);
////        this.rightBottom = new DrawableResource(resourceLocation, uRight, vBottom, rightWidth, bottomHeight);
////
////        this.width = width;
////        this.height = height;
////    }
////
////    public void setWidth(int width)
////    {
////        this.width = width;
////    }
////
////    public void setHeight(int height)
////    {
////        this.height = height;
////    }
////
////    @Override
////    public int getWidth()
////    {
////        return width;
////    }
////
////    @Override
////    public int getHeight()
////    {
////        return height;
////    }
////
////    @Override
////    public void draw(Minecraft minecraft, int xOffset, int yOffset)
////    {
////        // corners first
////        this.leftTop.draw(minecraft, xOffset, yOffset);
////        this.leftBottom.draw(minecraft, xOffset, yOffset + height - this.leftBottom.getHeight());
////        this.rightTop.draw(minecraft, xOffset + width - this.rightTop.getWidth(), yOffset);
////        this.rightBottom.draw(minecraft, xOffset + width - this.rightBottom.getWidth(), yOffset + height - this.rightBottom.getHeight());
////
////        // fill in the remaining areas
////        final int leftWidth = this.leftTop.getWidth();
////        final int rightWidth = this.rightTop.getWidth();
////        final int middleWidth = width - leftWidth - rightWidth;
////        final int topHeight = this.leftTop.getHeight();
////        final int bottomHeight = this.leftBottom.getHeight();
////        final int middleHeight = height - topHeight - bottomHeight;
////        if (middleWidth > 0)
////        {
////            drawTiled(minecraft, xOffset + leftWidth, yOffset, middleWidth, topHeight, this.middleTop);
////            drawTiled(minecraft, xOffset + leftWidth, yOffset + height - this.leftBottom.getHeight(), middleWidth, bottomHeight, this.middleBottom);
////        }
////        if (middleHeight > 0)
////        {
////            drawTiled(minecraft, xOffset, yOffset + topHeight, leftWidth, middleHeight, this.leftMiddle);
////            drawTiled(minecraft, xOffset + width - this.rightTop.getWidth(), yOffset + topHeight, rightWidth, middleHeight, this.rightMiddle);
////        }
////        if (middleHeight > 0 && middleWidth > 0)
////        {
////            drawTiled(minecraft, xOffset + leftWidth, yOffset + topHeight, middleWidth, middleHeight, this.middleMiddle);
////        }
////    }
////
////    private void drawTiled(Minecraft minecraft, final int xOffset, final int yOffset, final int tiledWidth, final int tiledHeight, IDrawableStatic drawable)
////    {
////        final int xTileCount = tiledWidth / drawable.getWidth();
////        final int xRemainder = tiledWidth - (xTileCount * drawable.getWidth());
////        final int yTileCount = tiledHeight / drawable.getHeight();
////        final int yRemainder = tiledHeight - (yTileCount * drawable.getHeight());
////
////        final int yStart = yOffset + tiledHeight;
////
////        for (int xTile = 0; xTile <= xTileCount; xTile++)
////        {
////            for (int yTile = 0; yTile <= yTileCount; yTile++)
////            {
////                int width = (xTile == xTileCount) ? xRemainder : drawable.getWidth();
////                int height = (yTile == yTileCount) ? yRemainder : drawable.getHeight();
////                int x = xOffset + (xTile * drawable.getWidth());
////                int y = yStart - ((yTile + 1) * drawable.getHeight());
////                if (width > 0 && height > 0)
////                {
////                    int maskTop = drawable.getHeight() - height;
////                    int maskRight = drawable.getWidth() - width;
////
////                    drawable.draw(minecraft, x, y, maskTop, 0, 0, maskRight);
////                }
////            }
////        }
////    }
////}
//
///*
//The MIT License (MIT)
//
//Copyright (c) 2014-2015 mezz
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.
// */
//
//package snownee.kiwi.client.gui.element;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.BufferBuilder;
//import net.minecraft.client.renderer.Tessellator;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.client.renderer.texture.TextureManager;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//import net.minecraft.util.ResourceLocation;
//import snownee.kiwi.client.KiwiSpriteUploader;
//
///**
// * Breaks a texture into 9 pieces so that it can be scaled to any size. Draws
// * the corners and then repeats any middle textures to fill the remaining area.
// */
//public class NineSliceDrawable implements IDrawable
//{
//    private final KiwiSpriteUploader spriteUploader;
//    private final ResourceLocation location;
//    private int width;
//    private int height;
//    private final int sliceLeft;
//    private final int sliceRight;
//    private final int sliceTop;
//    private final int sliceBottom;
//
//    public NineSliceDrawable(KiwiSpriteUploader spriteUploader, ResourceLocation location, int width, int height, int left, int right, int top, int bottom)
//    {
//        this.spriteUploader = spriteUploader;
//        this.location = location;
//
//        this.width = width;
//        this.height = height;
//        this.sliceLeft = left;
//        this.sliceRight = right;
//        this.sliceTop = top;
//        this.sliceBottom = bottom;
//    }
//
//    public void draw(int xOffset, int yOffset, int width, int height)
//    {
//        TextureAtlasSprite sprite = spriteUploader.getSprite(location);
//        int leftWidth = sliceLeft;
//        int rightWidth = sliceRight;
//        int topHeight = sliceTop;
//        int bottomHeight = sliceBottom;
//        int textureWidth = this.width;
//        int textureHeight = this.height;
//
//        Minecraft minecraft = Minecraft.getInstance();
//        TextureManager textureManager = minecraft.getTextureManager();
//        textureManager.bindTexture(spriteUploader.getLocation());
//
//        float uMin = sprite.getMinU();
//        float uMax = sprite.getMaxU();
//        float vMin = sprite.getMinV();
//        float vMax = sprite.getMaxV();
//        float uSize = uMax - uMin;
//        float vSize = vMax - vMin;
//
//        float uLeft = uMin + uSize * (leftWidth / (float) textureWidth);
//        float uRight = uMax - uSize * (rightWidth / (float) textureWidth);
//        float vTop = vMin + vSize * (topHeight / (float) textureHeight);
//        float vBottom = vMax - vSize * (bottomHeight / (float) textureHeight);
//
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder bufferBuilder = tessellator.getBuffer();
//        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
//
//        // left top
//        draw(bufferBuilder, uMin, vMin, uLeft, vTop, xOffset, yOffset, leftWidth, topHeight);
//        // left bottom
//        draw(bufferBuilder, uMin, vBottom, uLeft, vMax, xOffset, yOffset + height - bottomHeight, leftWidth, bottomHeight);
//        // right top
//        draw(bufferBuilder, uRight, vMin, uMax, vTop, xOffset + width - rightWidth, yOffset, rightWidth, topHeight);
//        // right bottom
//        draw(bufferBuilder, uRight, vBottom, uMax, vMax, xOffset + width - rightWidth, yOffset + height - bottomHeight, rightWidth, bottomHeight);
//
//        int middleWidth = textureWidth - leftWidth - rightWidth;
//        int middleHeight = textureWidth - topHeight - bottomHeight;
//        int tiledMiddleWidth = width - leftWidth - rightWidth;
//        int tiledMiddleHeight = height - topHeight - bottomHeight;
//        if (tiledMiddleWidth > 0)
//        {
//            // top edge
//            drawTiled(bufferBuilder, uLeft, vMin, uRight, vTop, xOffset + leftWidth, yOffset, tiledMiddleWidth, topHeight, middleWidth, topHeight);
//            // bottom edge
//            drawTiled(bufferBuilder, uLeft, vBottom, uRight, vMax, xOffset + leftWidth, yOffset + height - bottomHeight, tiledMiddleWidth, bottomHeight, middleWidth, bottomHeight);
//        }
//        if (tiledMiddleHeight > 0)
//        {
//            // left side
//            drawTiled(bufferBuilder, uMin, vTop, uLeft, vBottom, xOffset, yOffset + topHeight, leftWidth, tiledMiddleHeight, leftWidth, middleHeight);
//            // right side
//            drawTiled(bufferBuilder, uRight, vTop, uMax, vBottom, xOffset + width - rightWidth, yOffset + topHeight, rightWidth, tiledMiddleHeight, rightWidth, middleHeight);
//        }
//        if (tiledMiddleHeight > 0 && tiledMiddleWidth > 0)
//        {
//            // middle area
//            drawTiled(bufferBuilder, uLeft, vTop, uRight, vBottom, xOffset + leftWidth, yOffset + topHeight, tiledMiddleWidth, tiledMiddleHeight, middleWidth, middleHeight);
//        }
//
//        tessellator.draw();
//    }
//
//    private void drawTiled(BufferBuilder bufferBuilder, float uMin, float vMin, float uMax, float vMax, int xOffset, int yOffset, int tiledWidth, int tiledHeight, int width, int height)
//    {
//        int xTileCount = tiledWidth / width;
//        int xRemainder = tiledWidth - (xTileCount * width);
//        int yTileCount = tiledHeight / height;
//        int yRemainder = tiledHeight - (yTileCount * height);
//
//        int yStart = yOffset + tiledHeight;
//
//        float uSize = uMax - uMin;
//        float vSize = vMax - vMin;
//
//        for (int xTile = 0; xTile <= xTileCount; xTile++)
//        {
//            for (int yTile = 0; yTile <= yTileCount; yTile++)
//            {
//                int tileWidth = (xTile == xTileCount) ? xRemainder : width;
//                int tileHeight = (yTile == yTileCount) ? yRemainder : height;
//                int x = xOffset + (xTile * width);
//                int y = yStart - ((yTile + 1) * height);
//                if (tileWidth > 0 && tileHeight > 0)
//                {
//                    int maskRight = width - tileWidth;
//                    int maskTop = height - tileHeight;
//                    float uOffset = (maskRight / (float) width) * uSize;
//                    float vOffset = (maskTop / (float) height) * vSize;
//
//                    draw(bufferBuilder, uMin, vMin + vOffset, uMax - uOffset, vMax, x, y + maskTop, tileWidth, tileHeight);
//                }
//            }
//        }
//    }
//
//    private static void draw(BufferBuilder bufferBuilder, float minU, double minV, float maxU, float maxV, int xOffset, int yOffset, int width, int height)
//    {
// SEE: VertexConsumer
//        bufferBuilder.pos(xOffset, yOffset + height, 0).tex(minU, maxV).endVertex();
//        bufferBuilder.pos(xOffset + width, yOffset + height, 0).tex(maxU, maxV).endVertex();
//        bufferBuilder.pos(xOffset + width, yOffset, 0).tex(maxU, minV).endVertex();
//        bufferBuilder.pos(xOffset, yOffset, 0).tex(minU, minV).endVertex();
//    }
//
//    public void setWidth(int width)
//    {
//        this.width = width;
//    }
//
//    public void setHeight(int height)
//    {
//        this.height = height;
//    }
//
//    @Override
//    public int getWidth()
//    {
//        return width;
//    }
//
//    @Override
//    public int getHeight()
//    {
//        return height;
//    }
//
//    @Override
//    public void draw(int xOffset, int yOffset)
//    {
//        // TODO Auto-generated method stub
//
//    }
//}

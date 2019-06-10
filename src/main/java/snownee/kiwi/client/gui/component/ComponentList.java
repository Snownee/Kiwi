//package snownee.kiwi.client.gui.component;
//
//import org.lwjgl.input.Mouse;
//import org.lwjgl.opengl.GL11;
//
//import net.minecraft.client.gui.Gui;
//import net.minecraft.client.gui.GuiButton;
//import net.minecraft.client.gui.ScaledResolution;
//import net.minecraft.client.renderer.BufferBuilder;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.client.renderer.Tessellator;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//import net.minecraftforge.fml.client.config.GuiUtils;
//import snownee.kiwi.client.gui.GuiControl;
//
//public abstract class ComponentList extends Component
//{
//
//    protected final int screenWidth;
//    protected final int screenHeight;
//    protected int mouseX;
//    protected int mouseY;
//    protected int offsetX;
//    protected int offsetY;
//    private float initialMouseClickY = -2.0F;
//    private float scrollFactor;
//    protected float scrollDistance;
//    public int selectedIndex = -1;
//    protected int hoveringIndex = -1;
//    private long lastClickTime = 0L;
//    private boolean highlightSelected = true;
//    private boolean hasHeader;
//    private int headerHeight;
//    protected boolean captureMouse = true;
//    private int cacheContentHeight;
//    protected boolean drawBackground = true;
//    protected boolean drawScrollbar = true;
//
//    public ComponentList(GuiControl parent, int width, int height, int left, int top, int screenWidth, int screenHeight)
//    {
//        super(parent, width, height);
//        this.top = top;
//        this.left = left;
//        this.screenWidth = screenWidth;
//        this.screenHeight = screenHeight;
//    }
//
//    protected void setHeaderInfo(boolean hasHeader, int headerHeight)
//    {
//        this.hasHeader = hasHeader;
//        this.headerHeight = headerHeight;
//        if (!hasHeader)
//            this.headerHeight = 0;
//    }
//
//    protected void setDrawBackground(boolean drawBackground)
//    {
//        this.drawBackground = drawBackground;
//    }
//
//    protected void setDrawScrollBar(boolean drawScrollbar)
//    {
//        this.drawScrollbar = drawScrollbar;
//    }
//
//    protected abstract int getSize();
//
//    protected abstract int getSlotHeight(int index);
//
//    protected abstract void elementClicked(int index, int x, int y, boolean doubleClick);
//
//    protected int getContentHeight()
//    {
//        return cacheContentHeight;
//    }
//
//    protected abstract void drawBackground();
//
//    /**
//     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
//     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
//     */
//    protected abstract void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess);
//
//    /**
//     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
//     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
//     */
//    protected void drawHeader(int entryRight, int relativeY, Tessellator tess)
//    {
//    }
//
//    protected void clickHeader(int x, int y)
//    {
//    }
//
//    // @Deprecated // Unused, Remove in 1.9.3?
//    //    public int getHoveringSlotIndex(int x, int y)
//    //    {
//    //        int left = this.left + 1;
//    //        int right = this.left + this.listWidth - 7;
//    //        int relativeY = y - this.top - this.headerHeight + (int) this.scrollDistance;
//    //        int entryIndex = relativeY / this.slotHeight;
//    //        return x >= left && x <= right && entryIndex >= 0 && relativeY >= 0 && entryIndex < this.getSize() ? entryIndex
//    //                : -1;
//    //    }
//
//    private void applyScrollLimits()
//    {
//        int listHeight = this.getContentHeight() - height;
//
//        if (listHeight < 0)
//        {
//            // listHeight /= 2; // Horizontal align
//            listHeight = 0;
//        }
//
//        if (this.scrollDistance < 0.0F)
//        {
//            this.scrollDistance = 0.0F;
//        }
//
//        if (this.scrollDistance > listHeight)
//        {
//            this.scrollDistance = listHeight;
//        }
//    }
//
//    public void actionPerformed(GuiButton button)
//    {
//        if (button.enabled)
//        {
//        }
//    }
//
//    @Override
//    public void handleMouseInput(int mouseX, int mouseY)
//    {
//        boolean isHovering = mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height;
//        if (!isHovering)
//            return;
//
//        int scroll = Mouse.getEventDWheel();
//        if (scroll != 0)
//        {
//            this.scrollDistance += (-1 * scroll / 120.0F) * cacheContentHeight / getSize() / 2;
//        }
//    }
//
//    @Override
//    public void drawScreen(int offsetX, int offsetY, int relMouseX, int relMouseY, float partialTicks)
//    {
//        this.offsetX = left + offsetX;
//        this.offsetY = top + offsetY;
//        if (cacheContentHeight == 0)
//        {
//            cacheContentHeight();
//        }
//
//        this.mouseX = relMouseX;
//        this.mouseY = relMouseY;
//
//        this.drawBackground();
//
//        boolean isHovering = mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height;
//
//        if (!isHovering)
//        {
//            this.hoveringIndex = -1;
//        }
//
//        int listLength = this.getSize();
//        int scrollBarWidth = 6;
//        int scrollBarRight = this.left + this.width;
//        int scrollBarLeft = drawScrollbar ? scrollBarRight - scrollBarWidth : scrollBarRight;
//        int entryRight = scrollBarLeft - 1;
//        int border = 0;
//        boolean clicked = false;
//
//        if (Mouse.isButtonDown(0))
//        {
//            if (this.initialMouseClickY == -1.0F)
//            {
//                if (isHovering)
//                {
//                    int mouseListY = mouseY - top - this.headerHeight + (int) this.scrollDistance - border;
//
//                    if (mouseX >= left && mouseX <= entryRight && mouseListY >= 0)
//                    {
//                        if (mouseListY >= 0)
//                        {
//                            if (hoveringIndex >= 0 && hoveringIndex < listLength)
//                            {
//                                clicked = true;
//                            }
//                            //                            int y = 0;
//                            //                            for (int slotIndex = 0; slotIndex < listLength; ++slotIndex)
//                            //                            {
//                            //                                y += getSlotHeight(slotIndex);
//                            //                                if (mouseListY < y)
//                            //                                {
//                            //                                    this.elementClicked(slotIndex, slotIndex == this.selectedIndex
//                            //                                            && System.currentTimeMillis() - this.lastClickTime < 250L);
//                            //                                    this.selectedIndex = slotIndex;
//                            //                                    System.out.println(selectedIndex);
//                            //                                    this.lastClickTime = System.currentTimeMillis();
//                            //                                    break;
//                            //                                }
//                            //                            }
//                        }
//                        else
//                        {
//                            this.clickHeader(mouseX - left, mouseY - top + (int) this.scrollDistance - border);
//                        }
//                    }
//
//                    if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight)
//                    {
//                        this.scrollFactor = -1.0F;
//                        int scrollHeight = this.getContentHeight() - height - border;
//                        if (scrollHeight < 1)
//                            scrollHeight = 1;
//
//                        int var13 = (int) ((float) (height * height) / (float) this.getContentHeight());
//
//                        if (var13 < 32)
//                            var13 = 32;
//                        if (var13 > height - border * 2)
//                            var13 = height - border * 2;
//
//                        this.scrollFactor /= (float) (height - var13) / (float) scrollHeight;
//                    }
//                    else
//                    {
//                        this.scrollFactor = 1.0F;
//                    }
//
//                    this.initialMouseClickY = mouseY;
//                }
//                else
//                {
//                    this.initialMouseClickY = -2.0F;
//                }
//            }
//            else if (this.initialMouseClickY >= 0.0F)
//            {
//                this.scrollDistance -= (mouseY - this.initialMouseClickY) * this.scrollFactor;
//                this.initialMouseClickY = mouseY;
//            }
//        }
//        else
//        {
//            this.initialMouseClickY = -1.0F;
//        }
//
//        this.applyScrollLimits();
//
//        Tessellator tess = Tessellator.getInstance();
//        BufferBuilder worldr = tess.getBuffer();
//        worldr.setTranslation(offsetX, offsetY, 0);
//
//        ScaledResolution res = new ScaledResolution(parent.mc);
//        double scaleW = parent.mc.displayWidth / res.getScaledWidth_double();
//        double scaleH = parent.mc.displayHeight / res.getScaledHeight_double();
//        GL11.glEnable(GL11.GL_SCISSOR_TEST);
//        GL11.glScissor((int) (this.offsetX * scaleW),
//                (int) (parent.mc.displayHeight - ((offsetY + top + height) * scaleH)), (int) (width * scaleW),
//                (int) (height * scaleH));
//
//        if (drawBackground)
//        {
//            if (this.parent.mc.world != null)
//            {
//                this.drawGradientRect(left, top, left + width, top + height, 0xC0DDDDDD, 0xC0DDDDDD);
//            }
//            else // Draw dark dirt background
//            {
//                GlStateManager.disableLighting();
//                GlStateManager.disableFog();
//                this.parent.mc.renderEngine.bindTexture(Gui.OPTIONS_BACKGROUND);
//                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//                final float scale = 32.0F;
//                worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//                worldr.pos(left, top + height, 0.0D).tex(left / scale, (top + height + scrollDistance) / scale)
//                        .color(0x20, 0x20, 0x20, 0xFF).endVertex();
//                worldr.pos(left + width, top + height, 0.0D)
//                        .tex((left + width) / scale, (top + height + scrollDistance) / scale)
//                        .color(0x20, 0x20, 0x20, 0xFF).endVertex();
//                worldr.pos(left + width, top, 0.0D).tex((left + width) / scale, (top + scrollDistance) / scale)
//                        .color(0x20, 0x20, 0x20, 0xFF).endVertex();
//                worldr.pos(left, top, 0.0D).tex(left / scale, (this.top + scrollDistance) / scale)
//                        .color(0x20, 0x20, 0x20, 0xFF).endVertex();
//                tess.draw();
//            }
//        }
//
//        int baseY = this.top + border - (int) this.scrollDistance;
//        int extraHeight = (this.getContentHeight() + border) - height;
//        int contentHeight = 0;
//
//        if (this.hasHeader)
//        {
//            this.drawHeader(entryRight, baseY, tess);
//            contentHeight += headerHeight;
//        }
//
//        for (int slotIdx = 0; slotIdx < listLength; ++slotIdx)
//        {
//            int slotTop = baseY + contentHeight;
//            int sloltHeight = getSlotHeight(slotIdx);
//            contentHeight += sloltHeight;
//            int slotBuffer = sloltHeight - border;
//
//            if (slotTop <= this.top + this.height && slotTop + slotBuffer >= this.top)
//            {
//                if (isHovering && (extraHeight <= 0 || mouseX < scrollBarLeft) && mouseY >= slotTop
//                        && mouseY < slotTop + sloltHeight)
//                {
//                    if (clicked)
//                    {
//                        this.elementClicked(hoveringIndex, mouseX - left, mouseY - slotTop,
//                                hoveringIndex == this.selectedIndex
//                                        && System.currentTimeMillis() - this.lastClickTime < 250L);
//                        this.selectedIndex = hoveringIndex;
//                        this.lastClickTime = System.currentTimeMillis();
//                    }
//                    hoveringIndex = slotIdx;
//                }
//                //                if (this.highlightSelected && slotIdx == selectedIndex)
//                //                {
//                //                    int min = this.left;
//                //                    int max = entryRight;
//                //                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//                //                    GlStateManager.disableTexture2D();
//                //                    worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//                //                    worldr.pos(min, slotTop + slotBuffer + 2, 0).tex(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//                //                    worldr.pos(max, slotTop + slotBuffer + 2, 0).tex(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//                //                    worldr.pos(max, slotTop - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//                //                    worldr.pos(min, slotTop - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//                //                    worldr.pos(min + 1, slotTop + slotBuffer + 1, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0xFF)
//                //                            .endVertex();
//                //                    worldr.pos(max - 1, slotTop + slotBuffer + 1, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0xFF)
//                //                            .endVertex();
//                //                    worldr.pos(max - 1, slotTop - 1, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//                //                    worldr.pos(min + 1, slotTop - 1, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
//                //                    tess.draw();
//                //                    GlStateManager.enableTexture2D();
//                //                }
//
//                this.drawSlot(slotIdx, entryRight, slotTop, slotBuffer, tess);
//            }
//        }
//
//        cacheContentHeight = contentHeight;
//        GlStateManager.disableDepth();
//
//        worldr.setTranslation(offsetX, offsetY, 0);
//        if (drawScrollbar && extraHeight > 0) // Draw scroll bar
//        {
//            int scrollBarHeight = (height * height) / this.getContentHeight();
//
//            if (scrollBarHeight < 32)
//                scrollBarHeight = 32;
//
//            if (scrollBarHeight > height - border * 2)
//                scrollBarHeight = height - border * 2;
//
//            int barTop = (int) this.scrollDistance * (height - scrollBarHeight) / extraHeight + top;
//            if (barTop < top)
//            {
//                barTop = top;
//            }
//
//            GlStateManager.disableTexture2D();
//            if (drawBackground)
//            {
//                worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//                worldr.pos(scrollBarLeft, top + height, 0.0D).tex(0.0D, 1.0D).color(0xDD, 0xDD, 0xDD, 0xFF).endVertex();
//                worldr.pos(scrollBarRight, top + height, 0.0D).tex(1.0D, 1.0D).color(0xDD, 0xDD, 0xDD, 0xFF)
//                        .endVertex();
//                worldr.pos(scrollBarRight, top, 0.0D).tex(1.0D, 0.0D).color(0xDD, 0xDD, 0xDD, 0xFF).endVertex();
//                worldr.pos(scrollBarLeft, top, 0.0D).tex(0.0D, 0.0D).color(0xDD, 0xDD, 0xDD, 0xFF).endVertex();
//                tess.draw();
//            }
//            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//            worldr.pos(scrollBarLeft, barTop + scrollBarHeight, 0.0D).tex(0.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF)
//                    .endVertex();
//            worldr.pos(scrollBarRight, barTop + scrollBarHeight, 0.0D).tex(1.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF)
//                    .endVertex();
//            worldr.pos(scrollBarRight, barTop, 0.0D).tex(1.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//            worldr.pos(scrollBarLeft, barTop, 0.0D).tex(0.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
//            tess.draw();
//            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
//            worldr.pos(scrollBarLeft, barTop + scrollBarHeight - 1, 0.0D).tex(0.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF)
//                    .endVertex();
//            worldr.pos(scrollBarRight - 1, barTop + scrollBarHeight - 1, 0.0D).tex(1.0D, 1.0D)
//                    .color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//            worldr.pos(scrollBarRight - 1, barTop, 0.0D).tex(1.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//            worldr.pos(scrollBarLeft, barTop, 0.0D).tex(0.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
//            tess.draw();
//        }
//        worldr.setTranslation(0, 0, 0);
//
//        GlStateManager.enableTexture2D();
//        GlStateManager.shadeModel(GL11.GL_FLAT);
//        GlStateManager.enableAlpha();
//        GlStateManager.disableBlend();
//        GL11.glDisable(GL11.GL_SCISSOR_TEST);
//    }
//
//    protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2)
//    {
//        GuiUtils.drawGradientRect(0, left, top, right, bottom, color1, color2);
//    }
//
//    public void cacheContentHeight()
//    {
//        int listLenth = getSize();
//        int height = 0;
//        for (int i = 0; i < listLenth; ++i)
//        {
//            height += getSlotHeight(i);
//        }
//        cacheContentHeight = height;
//    }
//
//}

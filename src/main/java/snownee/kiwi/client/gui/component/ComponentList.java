package snownee.kiwi.client.gui.component;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.client.config.GuiUtils;
import snownee.kiwi.client.gui.GuiControl;

public abstract class ComponentList extends Component
{

    protected final int listWidth;
    protected final int screenWidth;
    protected final int screenHeight;
    protected final int top;
    protected final int bottom;
    protected final int right;
    protected final int left;
    protected int mouseX;
    protected int mouseY;
    protected int offsetX;
    protected int offsetY;
    private float initialMouseClickY = -2.0F;
    private float scrollFactor;
    protected float scrollDistance;
    protected int selectedIndex = -1;
    protected int hoveringIndex = -1;
    private long lastClickTime = 0L;
    private boolean highlightSelected = true;
    private boolean hasHeader;
    private int headerHeight;
    protected boolean captureMouse = true;
    private int cacheContentHeight;

    public ComponentList(GuiControl parent, int width, int top, int bottom, int left, int screenWidth, int screenHeight)
    {
        super(parent);
        this.listWidth = width;
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = width + this.left;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    protected void setHeaderInfo(boolean hasHeader, int headerHeight)
    {
        this.hasHeader = hasHeader;
        this.headerHeight = headerHeight;
        if (!hasHeader)
            this.headerHeight = 0;
    }

    protected abstract int getSize();

    protected abstract int getSlotHeight(int index);

    protected abstract void elementClicked(int index, boolean doubleClick);

    protected abstract boolean isSelected(int index);

    protected int getContentHeight()
    {
        return cacheContentHeight;
    }

    protected abstract void drawBackground();

    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     */
    protected abstract void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess);

    /**
     * Draw anything special on the screen. GL_SCISSOR is enabled for anything that
     * is rendered outside of the view box. Do not mess with SCISSOR unless you support this.
     */
    protected void drawHeader(int entryRight, int relativeY, Tessellator tess)
    {
    }

    protected void clickHeader(int x, int y)
    {
    }

    // @Deprecated // Unused, Remove in 1.9.3?
    //    public int getHoveringSlotIndex(int x, int y)
    //    {
    //        int left = this.left + 1;
    //        int right = this.left + this.listWidth - 7;
    //        int relativeY = y - this.top - this.headerHeight + (int) this.scrollDistance;
    //        int entryIndex = relativeY / this.slotHeight;
    //        return x >= left && x <= right && entryIndex >= 0 && relativeY >= 0 && entryIndex < this.getSize() ? entryIndex
    //                : -1;
    //    }

    private void applyScrollLimits()
    {
        int listHeight = this.getContentHeight() - (this.bottom - this.top);

        if (listHeight < 0)
        {
            // listHeight /= 2; // Horizontal align
            listHeight = 0;
        }

        if (this.scrollDistance < 0.0F)
        {
            this.scrollDistance = 0.0F;
        }

        if (this.scrollDistance > (float) listHeight)
        {
            this.scrollDistance = (float) listHeight;
        }
    }

    public void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
        }
    }

    @Override
    public void handleMouseInput(int mouseX, int mouseY)
    {
        boolean isHovering = mouseX >= this.left && mouseX <= this.left + this.listWidth && mouseY >= this.top
                && mouseY <= this.bottom;
        if (!isHovering)
            return;

        int scroll = Mouse.getEventDWheel();
        if (scroll != 0)
        {
            this.scrollDistance += (float) ((-1 * scroll / 120.0F) * cacheContentHeight / getSize() / 2);
        }
    }

    @Override
    public void drawScreen(int offsetX, int offsetY, int relMouseX, int relMouseY, float partialTicks)
    {
        this.offsetX = left + offsetX;
        this.offsetY = top + offsetY;
        if (cacheContentHeight == 0)
        {
            cacheContentHeight();
        }

        this.mouseX = relMouseX;
        this.mouseY = relMouseY;

        this.drawBackground();

        boolean isHovering = mouseX >= this.left && mouseX <= this.left + this.listWidth && mouseY >= this.top
                && mouseY <= this.bottom;

        if (!isHovering)
        {
            this.hoveringIndex = -1;
        }

        int listLength = this.getSize();
        int scrollBarWidth = 6;
        int scrollBarRight = this.left + this.listWidth;
        int scrollBarLeft = scrollBarRight - scrollBarWidth;
        int entryLeft = this.left;
        int entryRight = scrollBarLeft - 1;
        int viewHeight = this.bottom - this.top;
        int border = 0;

        if (Mouse.isButtonDown(0))
        {
            if (this.initialMouseClickY == -1.0F)
            {
                if (isHovering)
                {
                    int mouseListY = mouseY - this.top - this.headerHeight + (int) this.scrollDistance - border;

                    if (mouseX >= entryLeft && mouseX <= entryRight && mouseListY >= 0)
                    {
                        if (mouseListY >= 0)
                        {
                            if (hoveringIndex >= 0 && hoveringIndex < listLength)
                            {
                                this.elementClicked(hoveringIndex, hoveringIndex == this.selectedIndex
                                        && System.currentTimeMillis() - this.lastClickTime < 250L);
                                this.selectedIndex = hoveringIndex;
                                this.lastClickTime = System.currentTimeMillis();
                            }
                            //                            int y = 0;
                            //                            for (int slotIndex = 0; slotIndex < listLength; ++slotIndex)
                            //                            {
                            //                                y += getSlotHeight(slotIndex);
                            //                                if (mouseListY < y)
                            //                                {
                            //                                    this.elementClicked(slotIndex, slotIndex == this.selectedIndex
                            //                                            && System.currentTimeMillis() - this.lastClickTime < 250L);
                            //                                    this.selectedIndex = slotIndex;
                            //                                    System.out.println(selectedIndex);
                            //                                    this.lastClickTime = System.currentTimeMillis();
                            //                                    break;
                            //                                }
                            //                            }
                        }
                        else
                        {
                            this.clickHeader(mouseX - entryLeft,
                                    mouseY - this.top + (int) this.scrollDistance - border);
                        }
                    }

                    if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight)
                    {
                        this.scrollFactor = -1.0F;
                        int scrollHeight = this.getContentHeight() - viewHeight - border;
                        if (scrollHeight < 1)
                            scrollHeight = 1;

                        int var13 = (int) ((float) (viewHeight * viewHeight) / (float) this.getContentHeight());

                        if (var13 < 32)
                            var13 = 32;
                        if (var13 > viewHeight - border * 2)
                            var13 = viewHeight - border * 2;

                        this.scrollFactor /= (float) (viewHeight - var13) / (float) scrollHeight;
                    }
                    else
                    {
                        this.scrollFactor = 1.0F;
                    }

                    this.initialMouseClickY = mouseY;
                }
                else
                {
                    this.initialMouseClickY = -2.0F;
                }
            }
            else if (this.initialMouseClickY >= 0.0F)
            {
                this.scrollDistance -= ((float) mouseY - this.initialMouseClickY) * this.scrollFactor;
                this.initialMouseClickY = (float) mouseY;
            }
        }
        else
        {
            this.initialMouseClickY = -1.0F;
        }

        this.applyScrollLimits();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder worldr = tess.getBuffer();
        worldr.setTranslation(offsetX, offsetY, 0);

        ScaledResolution res = new ScaledResolution(parent.mc);
        double scaleW = parent.mc.displayWidth / res.getScaledWidth_double();
        double scaleH = parent.mc.displayHeight / res.getScaledHeight_double();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (this.offsetX * scaleW), (int) (parent.mc.displayHeight - ((offsetY + bottom) * scaleH)),
                (int) (listWidth * scaleW), (int) (viewHeight * scaleH));

        if (this.parent.mc.world != null)
        {
            this.drawGradientRect(left, top, right, bottom, 0xC0DDDDDD, 0xC0DDDDDD);
        }
        else // Draw dark dirt background
        {
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            this.parent.mc.renderEngine.bindTexture(Gui.OPTIONS_BACKGROUND);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            final float scale = 32.0F;
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(this.left, this.bottom, 0.0D)
                    .tex(this.left / scale, (this.bottom + (int) this.scrollDistance) / scale)
                    .color(0x20, 0x20, 0x20, 0xFF).endVertex();
            worldr.pos(this.right, this.bottom, 0.0D)
                    .tex(this.right / scale, (this.bottom + (int) this.scrollDistance) / scale)
                    .color(0x20, 0x20, 0x20, 0xFF).endVertex();
            worldr.pos(this.right, this.top, 0.0D)
                    .tex(this.right / scale, (this.top + (int) this.scrollDistance) / scale)
                    .color(0x20, 0x20, 0x20, 0xFF).endVertex();
            worldr.pos(this.left, this.top, 0.0D).tex(this.left / scale, (this.top + (int) this.scrollDistance) / scale)
                    .color(0x20, 0x20, 0x20, 0xFF).endVertex();
            tess.draw();
        }

        int baseY = this.top + border - (int) this.scrollDistance;
        int extraHeight = (this.getContentHeight() + border) - viewHeight;
        int contentHeight = 0;

        if (this.hasHeader)
        {
            this.drawHeader(entryRight, baseY, tess);
            contentHeight += headerHeight;
        }

        for (int slotIdx = 0; slotIdx < listLength; ++slotIdx)
        {
            int slotTop = baseY + contentHeight;
            int sloltHeight = getSlotHeight(slotIdx);
            contentHeight += sloltHeight;
            int slotBuffer = sloltHeight - border;

            if (slotTop <= this.bottom && slotTop + slotBuffer >= this.top)
            {
                if (isHovering && (extraHeight <= 0 || mouseX < scrollBarLeft) && mouseY >= slotTop
                        && mouseY < slotTop + sloltHeight)
                {
                    hoveringIndex = slotIdx;
                }
                if (this.highlightSelected && this.isSelected(slotIdx))
                {
                    int min = this.left;
                    int max = entryRight;
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableTexture2D();
                    worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                    worldr.pos(min, slotTop + slotBuffer + 2, 0).tex(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(max, slotTop + slotBuffer + 2, 0).tex(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(max, slotTop - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(min, slotTop - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(min + 1, slotTop + slotBuffer + 1, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0xFF)
                            .endVertex();
                    worldr.pos(max - 1, slotTop + slotBuffer + 1, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0xFF)
                            .endVertex();
                    worldr.pos(max - 1, slotTop - 1, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    worldr.pos(min + 1, slotTop - 1, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    tess.draw();
                    GlStateManager.enableTexture2D();
                }

                this.drawSlot(slotIdx, entryRight, slotTop, slotBuffer, tess);
            }
        }

        cacheContentHeight = contentHeight;
        GlStateManager.disableDepth();

        if (extraHeight > 0) // Draw scroll bar
        {
            int height = (viewHeight * viewHeight) / this.getContentHeight();

            if (height < 32)
                height = 32;

            if (height > viewHeight - border * 2)
                height = viewHeight - border * 2;

            int barTop = (int) this.scrollDistance * (viewHeight - height) / extraHeight + this.top;
            if (barTop < this.top)
            {
                barTop = this.top;
            }

            GlStateManager.disableTexture2D();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(scrollBarLeft, this.bottom, 0.0D).tex(0.0D, 1.0D).color(0xDD, 0xDD, 0xDD, 0xFF).endVertex();
            worldr.pos(scrollBarRight, this.bottom, 0.0D).tex(1.0D, 1.0D).color(0xDD, 0xDD, 0xDD, 0xFF).endVertex();
            worldr.pos(scrollBarRight, this.top, 0.0D).tex(1.0D, 0.0D).color(0xDD, 0xDD, 0xDD, 0xFF).endVertex();
            worldr.pos(scrollBarLeft, this.top, 0.0D).tex(0.0D, 0.0D).color(0xDD, 0xDD, 0xDD, 0xFF).endVertex();
            tess.draw();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(scrollBarLeft, barTop + height, 0.0D).tex(0.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(scrollBarRight, barTop + height, 0.0D).tex(1.0D, 1.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(scrollBarRight, barTop, 0.0D).tex(1.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            worldr.pos(scrollBarLeft, barTop, 0.0D).tex(0.0D, 0.0D).color(0x80, 0x80, 0x80, 0xFF).endVertex();
            tess.draw();
            worldr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldr.pos(scrollBarLeft, barTop + height - 1, 0.0D).tex(0.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF)
                    .endVertex();
            worldr.pos(scrollBarRight - 1, barTop + height - 1, 0.0D).tex(1.0D, 1.0D).color(0xC0, 0xC0, 0xC0, 0xFF)
                    .endVertex();
            worldr.pos(scrollBarRight - 1, barTop, 0.0D).tex(1.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            worldr.pos(scrollBarLeft, barTop, 0.0D).tex(0.0D, 0.0D).color(0xC0, 0xC0, 0xC0, 0xFF).endVertex();
            tess.draw();
        }
        worldr.setTranslation(0, 0, 0);

        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2)
    {
        GuiUtils.drawGradientRect(0, left, top, right, bottom, color1, color2);
    }

    public void cacheContentHeight()
    {
        int listLenth = getSize();
        int height = 0;
        for (int i = 0; i < listLenth; ++i)
        {
            height += getSlotHeight(i);
        }
        cacheContentHeight = height;
    }

}

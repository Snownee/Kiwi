package snownee.kiwi.client.gui;

import java.io.IOException;
import java.util.List;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import snownee.kiwi.client.gui.component.Component;

public abstract class GuiContainerMod extends GuiContainer implements IMessageHandler
{
    public GuiControl control;
    protected List<String> tooltip;
    protected FontRenderer tooltipFont;

    public GuiContainerMod(Container container)
    {
        super(container);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void initGui()
    {
        super.initGui();
        this.control = new GuiControl(mc, width, height, this);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        control.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (tooltip != null && !tooltip.isEmpty())
        {
            if (tooltipFont == null)
            {
                tooltipFont = fontRenderer;
            }
            drawHoveringText(tooltip, mouseX, mouseY, tooltipFont);
        }
        this.tooltip = null;
        this.tooltipFont = null;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);
        control.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        super.handleMouseInput();
        control.handleMouseInput(mouseX, mouseY);
    }

    @Override
    public void setTooltip(GuiControl control, Component component, List<String> tooltip, FontRenderer fontRenderer)
    {
        this.tooltip = tooltip;
        this.tooltipFont = fontRenderer;
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        control.onDestroy();
    }
}

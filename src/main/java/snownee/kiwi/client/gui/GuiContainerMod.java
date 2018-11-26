package snownee.kiwi.client.gui;

import java.io.IOException;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

public abstract class GuiContainerMod extends GuiContainer implements IMessageHandler
{
    public GuiControl control;

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
    public void onGuiClosed()
    {
        super.onGuiClosed();
        control.onDestroy();
    }
}

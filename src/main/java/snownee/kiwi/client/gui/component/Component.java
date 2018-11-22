package snownee.kiwi.client.gui.component;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import snownee.kiwi.client.gui.GuiControl;

public abstract class Component
{
    private int zLevel;
    public int width;
    public int height;
    public int top;
    public int left;
    protected GuiControl parent;

    public Component(GuiControl parent, int width, int height)
    {
        this.parent = parent;
        this.width = width;
        this.height = height;
    }

    public int getZLevel()
    {
        return zLevel;
    }

    public abstract void drawScreen(int offsetX, int offsetY, int relMouseX, int relMouseY, float partialTicks2);

    public void keyTyped(char typedChar, int keyCode)
    {
    }

    public void handleMouseInput(int relMouseX, int relMouseY)
    {
    }

    @OverridingMethodsMustInvokeSuper
    public void onDestroy()
    {
        parent = null;
    }
}

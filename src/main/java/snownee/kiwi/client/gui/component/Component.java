package snownee.kiwi.client.gui.component;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import snownee.kiwi.client.gui.GuiControl;

public abstract class Component
{
    private int zLevel;
    protected GuiControl parent;

    public Component(GuiControl parent)
    {
        this.parent = parent;
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

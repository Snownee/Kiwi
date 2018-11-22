package snownee.kiwi.client.gui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import net.minecraft.client.Minecraft;
import snownee.kiwi.client.gui.component.Component;

public class GuiControl
{
    private final List<Component> components = new ArrayList<>();
    public Minecraft mc;
    public int offsetX;
    public int offsetY;
    public int width;
    public int height;

    public GuiControl(Minecraft mc, int width, int height)
    {
        this.mc = mc;
        this.width = width;
        this.height = height;
    }

    public void addComponent(Component component)
    {
        boolean flag = true;
        for (int i = 0; i < components.size(); ++i)
        {
            Component c = components.get(i);
            if (component.getZLevel() >= c.getZLevel())
            {
                components.add(i, component);
                flag = true;
                break;
            }
        }
        if (flag)
        {
            components.add(component);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        for (Component c : components)
        {
            c.drawScreen(offsetX, offsetY, mouseX - offsetX, mouseY - offsetY, partialTicks);
        }
    }

    public void keyTyped(char typedChar, int keyCode)
    {
        for (Component c : components)
        {
            c.keyTyped(typedChar, keyCode);
        }
    }

    public void handleMouseInput(int mouseX, int mouseY)
    {
        for (Component c : components)
        {
            c.handleMouseInput(mouseX - offsetX, mouseY - offsetY);
        }
    }

    @OverridingMethodsMustInvokeSuper
    public void onDestroy()
    {
        for (Component c : components)
        {
            c.onDestroy();
        }
        components.clear();
        this.mc = null;
    }

}

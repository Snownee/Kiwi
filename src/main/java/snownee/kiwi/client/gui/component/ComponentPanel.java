package snownee.kiwi.client.gui.component;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import snownee.kiwi.client.gui.GuiControl;
import snownee.kiwi.client.gui.element.DrawableNineSlice;

public class ComponentPanel extends Component
{
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/recipe_book.png");
    private final DrawableNineSlice background;
    public final GuiControl control;

    public ComponentPanel(GuiControl parent, int width, int height)
    {
        super(parent);
        background = new DrawableNineSlice(TEXTURE, 82, 208, 32, 32, 4, 4, 4, 4);
        background.setHeight(height);
        background.setWidth(width);
        control = new GuiControl(parent.mc, width - 8, height - 8);
        control.offsetX = (parent.width - width) / 2 + 4;
        control.offsetY = (parent.height - height) / 2 - 4;
    }

    @Override
    public void drawScreen(int offsetX, int offsetY, int relMouseX, int relMouseY, float partialTicks)
    {
        GlStateManager.color(1, 1, 1, 1);
        background.draw(parent.mc, offsetX + control.offsetX - 4, offsetY + control.offsetY - 4);
        control.drawScreen(relMouseX, relMouseY, partialTicks);
    }

    @Override
    public void handleMouseInput(int mouseX, int mouseY)
    {
        control.handleMouseInput(mouseX, mouseY);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode)
    {
        control.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onDestroy()
    {
        control.onDestroy();
        super.onDestroy();
    }

}

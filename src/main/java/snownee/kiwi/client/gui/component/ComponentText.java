package snownee.kiwi.client.gui.component;

import java.util.Collections;
import java.util.List;

import snownee.kiwi.client.AdvancedFontRenderer;
import snownee.kiwi.client.gui.GuiControl;
import snownee.kiwi.client.gui.component.Component;

public class ComponentText extends Component
{
    private List<String> lines = Collections.EMPTY_LIST;
    public int baseColor = 0;
    public boolean dropShadow = false;
    public int paddingX;
    public int paddingY;

    public ComponentText(GuiControl parent, int width)
    {
        this(parent, width, 0, 0);
    }

    public ComponentText(GuiControl parent, int width, int paddingX, int paddingY)
    {
        super(parent, width, AdvancedFontRenderer.INSTANCE.FONT_HEIGHT + 1);
        this.paddingX = paddingX;
        this.paddingY = paddingY;
    }

    public void setText(String text)
    {
        lines = AdvancedFontRenderer.INSTANCE.listFormattedStringToWidth(text, width - paddingX * 2);
        height = paddingY * 2 + lines.size() * (AdvancedFontRenderer.INSTANCE.FONT_HEIGHT + 1);
    }

    public List<String> getLines()
    {
        return Collections.unmodifiableList(lines);
    }

    @Override
    public void drawScreen(int offsetX, int offsetY, int relMouseX, int relMouseY, float partialTicks)
    {
        int x = offsetX + left + paddingX;
        int y = offsetY + top + paddingY;
        for (String line : lines)
        {
            AdvancedFontRenderer.INSTANCE.drawString(line, x, y, baseColor, dropShadow);
            y += AdvancedFontRenderer.INSTANCE.FONT_HEIGHT + 1;
        }
    }

}

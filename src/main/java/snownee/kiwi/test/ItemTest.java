package snownee.kiwi.test;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import snownee.kiwi.client.AdvancedFontRenderer;
import snownee.kiwi.item.ItemMod;

public class ItemTest extends ItemMod
{
    public ItemTest(String name)
    {
        super(name);
    }

    @Override
    public FontRenderer getFontRenderer(ItemStack stack)
    {
        return AdvancedFontRenderer.INSTANCE;
    }
}

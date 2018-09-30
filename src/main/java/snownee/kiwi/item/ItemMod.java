package snownee.kiwi.item;

import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiConfig;

public class ItemMod extends Item implements IModItem
{
    private final String name;

    public ItemMod(String name)
    {
        super();
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void register(String modid)
    {
        setRegistryName(modid, getName());
        setTranslationKey(modid + "." + getName());
    }

    @Override
    public Item cast()
    {
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        addTip(stack, tooltip);
    }

    @SideOnly(Side.CLIENT)
    public static void addTip(ItemStack stack, List<String> tooltip)
    {
        if (I18n.hasKey(stack.getTranslationKey() + ".tip"))
        {
            if (!KiwiConfig.GENERAL.tooltipRequiresShift || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
            {
                FontRenderer fontRenderer = stack.getItem().getFontRenderer(stack);
                if (fontRenderer == null)
                {
                    fontRenderer = Minecraft.getMinecraft().fontRenderer;
                }
                tooltip.addAll(fontRenderer.listFormattedStringToWidth(I18n.format(stack.getTranslationKey() + ".tip"), KiwiConfig.GENERAL.tooltipWrapWidth));
            }
            else if (KiwiConfig.GENERAL.tooltipRequiresShift)
            {
                tooltip.add(I18n.format(Kiwi.MODID + ".tip.press_shift"));
            }
        }
    }
}

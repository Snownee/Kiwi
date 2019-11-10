package snownee.kiwi.item;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.KiwiConfig;

public class ModItem extends Item {
    public ModItem(Item.Properties builder) {
        super(builder);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        addTip(stack, worldIn, tooltip, flagIn);
    }

    @OnlyIn(Dist.CLIENT)
    public static void addTip(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (tooltip.size() > 0 && I18n.hasKey(stack.getTranslationKey() + ".tip")) {
            if (!KiwiConfig.tooltipRequiresShift.get() || Screen.hasShiftDown()) {
                FontRenderer fontRenderer = stack.getItem().getFontRenderer(stack);
                if (fontRenderer == null) {
                    fontRenderer = Minecraft.getInstance().fontRenderer;
                }
                int width = fontRenderer.getStringWidth(tooltip.get(0).getFormattedText());
                /* off */
                tooltip.addAll(
                        fontRenderer.listFormattedStringToWidth(I18n.format(stack.getTranslationKey() + ".tip"), Math.max(width, KiwiConfig.tooltipWrapWidth.get()))
                        .stream()
                        .map(StringTextComponent::new)
                        .collect(Collectors.toList()));
                /* on */
            } else if (KiwiConfig.tooltipRequiresShift.get()) {
                tooltip.add(new TranslationTextComponent("tip.kiwi.press_shift"));
            }
        }
        if (flagIn == TooltipFlags.ADVANCED && stack.hasTag() && Screen.hasShiftDown()) {
            tooltip.add(new StringTextComponent(stack.getTag().toString()));
        }
    }
}

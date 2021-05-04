package snownee.kiwi.item;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.KiwiClientConfig;

public class ModItem extends Item {
	public ModItem(Item.Properties builder) {
		super(builder);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (!KiwiClientConfig.globalTooltip)
			addTip(stack, tooltip, flagIn);
	}

	@OnlyIn(Dist.CLIENT)
	public static void addTip(ItemStack stack, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (tooltip.isEmpty()) {
			return;
		}
		String key;
		boolean shift = Screen.hasShiftDown();
		boolean ctrl = Screen.hasControlDown();
		if (shift == ctrl) {
			key = stack.getTranslationKey() + ".tip";
		} else if (shift) {
			key = stack.getTranslationKey() + ".tip.shift";
		} else { // ctrl
			key = stack.getTranslationKey() + ".tip.ctrl";
		}
		boolean hasKey = I18n.hasKey(key);
		if (!hasKey && (shift != ctrl)) {
			return;
		}
		if (hasKey) {
			List<String> lines = Lists.newArrayList(I18n.format(key).split("\n"));

			FontRenderer fontRenderer = stack.getItem().getFontRenderer(stack);
			if (fontRenderer == null) {
				fontRenderer = Minecraft.getInstance().fontRenderer;
			}
			FontRenderer fontRenderer2 = fontRenderer;
			int width = Math.max(fontRenderer.getStringWidth(tooltip.get(0).getString()), KiwiClientConfig.tooltipWrapWidth1);
			/* off */
            tooltip.addAll(
                    lines.stream()
                    .map(s -> fontRenderer2.getCharacterManager().func_238365_g_(s, width, Style.EMPTY))
                    .flatMap(Collection::stream)
                    .map(ITextProperties::getString)
                    .map(StringTextComponent::new)
                    .peek(c -> c.mergeStyle(TextFormatting.GRAY)) //FIXME: Style is empty after wrapping line
                    .collect(Collectors.toList())
            );
            /* on */
		}
		if (shift == ctrl) {
			boolean hasShiftKey = I18n.hasKey(key + ".shift");
			boolean hasCtrlKey = I18n.hasKey(key + ".ctrl");
			if (hasShiftKey && hasCtrlKey) {
				tooltip.add(new TranslationTextComponent("tip.kiwi.press_shift_or_ctrl"));
			} else if (hasShiftKey) {
				tooltip.add(new TranslationTextComponent("tip.kiwi.press_shift"));
			} else if (hasCtrlKey) {
				tooltip.add(new TranslationTextComponent("tip.kiwi.press_ctrl"));
			}
		}
	}
}

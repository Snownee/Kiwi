package snownee.kiwi.item;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.KiwiClientConfig;

public class ModItem extends Item {
	public ModItem(Item.Properties builder) {
		super(builder);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (!KiwiClientConfig.globalTooltip)
			addTip(stack, tooltip, flagIn);
	}

	@OnlyIn(Dist.CLIENT)
	public static void addTip(ItemStack stack, List<Component> tooltip, TooltipFlag flagIn) {
		if (tooltip.isEmpty()) {
			return;
		}
		String key;
		boolean shift = Screen.hasShiftDown();
		boolean ctrl = Screen.hasControlDown();
		if (shift == ctrl) {
			key = stack.getDescriptionId() + ".tip";
		} else if (shift) {
			key = stack.getDescriptionId() + ".tip.shift";
		} else { // ctrl
			key = stack.getDescriptionId() + ".tip.ctrl";
		}
		boolean hasKey = I18n.exists(key);
		if (!hasKey && (shift != ctrl)) {
			return;
		}
		if (hasKey) {
			List<String> lines = Lists.newArrayList(I18n.get(key).split("\n"));

			Font fontRenderer = Minecraft.getInstance().font;
			int width = Math.max(fontRenderer.width(tooltip.get(0).getString()), KiwiClientConfig.tooltipWrapWidth1);
			/* off */
            tooltip.addAll(
                    lines.stream()
                    .map(s -> fontRenderer.getSplitter().splitLines(s, width, Style.EMPTY))
                    .flatMap(Collection::stream)
                    .map(FormattedText::getString)
                    .map(TextComponent::new)
                    .peek(c -> c.withStyle(ChatFormatting.GRAY)) //FIXME: Style is empty after wrapping line
                    .collect(Collectors.toList())
            );
            /* on */
		}
		if (shift == ctrl) {
			boolean hasShiftKey = I18n.exists(key + ".shift");
			boolean hasCtrlKey = I18n.exists(key + ".ctrl");
			if (hasShiftKey && hasCtrlKey) {
				tooltip.add(new TranslatableComponent("tip.kiwi.press_shift_or_ctrl"));
			} else if (hasShiftKey) {
				tooltip.add(new TranslatableComponent("tip.kiwi.press_shift"));
			} else if (hasCtrlKey) {
				tooltip.add(new TranslatableComponent("tip.kiwi.press_ctrl"));
			}
		}
	}
}

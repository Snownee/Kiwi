package snownee.kiwi.item;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import snownee.kiwi.KiwiClientConfig;

public class ModItem extends Item {
	public ModItem(Item.Properties builder) {
		super(builder);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (!KiwiClientConfig.globalTooltip)
			addTip(stack, tooltip, flagIn);
	}

	@Environment(EnvType.CLIENT)
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
			/* off */
            tooltip.addAll(
                    lines.stream()
                    .map(Component::literal)
                    .peek(c -> c.withStyle(ChatFormatting.GRAY))
                    .toList()
            );
            /* on */
		}
		if (shift == ctrl) {
			boolean hasShiftKey = I18n.exists(key + ".shift");
			boolean hasCtrlKey = I18n.exists(key + ".ctrl");
			if (hasShiftKey && hasCtrlKey) {
				tooltip.add(Component.translatable("tip.kiwi.press_shift_or_ctrl"));
			} else if (hasShiftKey) {
				tooltip.add(Component.translatable("tip.kiwi.press_shift"));
			} else if (hasCtrlKey) {
				tooltip.add(Component.translatable("tip.kiwi.press_ctrl"));
			}
		}
	}
}

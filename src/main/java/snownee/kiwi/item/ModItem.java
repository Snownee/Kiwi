package snownee.kiwi.item;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.loader.Platform;

public class ModItem extends Item {
	public ModItem(Properties builder) {
		super(builder);
	}

	@Override
	public void appendHoverText(
			ItemStack itemStack,
			TooltipContext tooltipContext,
			TooltipDisplay tooltipDisplay,
			Consumer<Component> tooltipAdder,
			TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, tooltipContext, tooltipDisplay, tooltipAdder, tooltipFlag);
		if (Platform.isPhysicalClient() && !KiwiClientConfig.globalTooltip) {
			List<Component> tooltip = Lists.newArrayList();
			ModItem.addTip(itemStack, tooltip, tooltipFlag);
			tooltip.forEach(tooltipAdder);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void addTip(ItemStack stack, List<Component> tooltip, TooltipFlag flagIn) {
		if (tooltip.isEmpty()) {
			return;
		}
		String key;
		boolean shift = flagIn.hasShiftDown();
		boolean ctrl = flagIn.hasControlDown();
		if (shift == ctrl) {
			key = stack.getItem().getDescriptionId() + ".tip";
		} else if (shift) {
			key = stack.getItem().getDescriptionId() + ".tip.shift";
		} else { // ctrl
			key = stack.getItem().getDescriptionId() + ".tip.ctrl";
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

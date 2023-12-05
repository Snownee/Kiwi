package snownee.kiwi.client;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.item.ModItem;

@Environment(EnvType.CLIENT)
public final class TooltipEvents {
	private TooltipEvents() {
	}

	private static ItemStack lastStack;
	private static CompoundTag lastNBT;
	private static Component lastFormatted;
	private static boolean firstSeenDebugTooltip = true;
	public static final String disableDebugTooltipCommand = "@kiwi disable debugTooltip";

	public static void globalTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
		if (KiwiClientConfig.globalTooltip)
			ModItem.addTip(stack, tooltip, flag);
	}

	public static void debugTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
		if (!Kiwi.areTagsUpdated() || !flag.isAdvanced()) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (stack != lastStack && minecraft.player != null && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292/*F3*/)) {
			lastStack = stack;
			CompoundTag data = stack.getTag();
			MutableComponent itextcomponent = Component.literal(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
			if (minecraft.keyboardHandler != null) {
				minecraft.keyboardHandler.setClipboard(itextcomponent.getString());
			}
			if (data != null) {
				itextcomponent.append(NbtUtils.toPrettyComponent(data));
			}
			itextcomponent.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, itextcomponent.getString())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click"))).withInsertion(itextcomponent.getString()));
			minecraft.player.displayClientMessage(itextcomponent, false);
			minecraft.gui.getDebugOverlay().toggleOverlay();
		}

		if (KiwiClientConfig.nbtTooltip && Screen.hasShiftDown() && stack.hasTag()) {
			trySendTipMsg(minecraft);
			tooltip.removeIf(c -> c.getContents() instanceof TranslatableContents && "item.nbt_tags".equals(((TranslatableContents) c.getContents()).getKey()));
			if (lastNBT != stack.getTag()) {
				lastNBT = stack.getTag();
				lastFormatted = NbtUtils.toPrettyComponent(lastNBT);
			}
			tooltip.add(lastFormatted);
		} else if (KiwiClientConfig.tagsTooltip) {
			List<String> tags = stack.getTags().map(TagKey::location).map(Object::toString).sorted().toList();
			if (!tags.isEmpty()) {
				trySendTipMsg(minecraft);
				tags.forEach(id -> {
					tooltip.add(Component.literal("#" + id).withStyle(ChatFormatting.DARK_GRAY));
				});
			}
		}
	}

	private static void trySendTipMsg(Minecraft mc) {
		if (firstSeenDebugTooltip && mc.player != null) {
			firstSeenDebugTooltip = false;
			if (KiwiClientConfig.debugTooltipMsg) {
				MutableComponent clickHere = Component.translatable("tip.kiwi.click_here").withStyle($ -> $.withClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, disableDebugTooltipCommand)));
				mc.player.sendSystemMessage(Component.translatable("tip.kiwi.debug_tooltip", clickHere.withStyle(ChatFormatting.AQUA)));
				KiwiClientConfig.debugTooltipMsg = false;
				KiwiConfigManager.getHandler(KiwiClientConfig.class).save();
			}
		}
	}
}

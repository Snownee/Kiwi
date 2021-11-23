package snownee.kiwi.client;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Function;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.item.ModItem;

@OnlyIn(Dist.CLIENT)
public final class TooltipEvents {
	private TooltipEvents() {
	}

	private static ItemStack lastStack;
	private static CompoundTag lastNBT;
	private static Component lastFormatted;
	private static Function<CompoundTag, Component> formatter;

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
			MutableComponent itextcomponent = new TextComponent(stack.getItem().getRegistryName().toString());
			if (minecraft.keyboardHandler != null) {
				minecraft.keyboardHandler.setClipboard(itextcomponent.getString());
			}
			if (data != null) {
				itextcomponent.append(NbtUtils.prettyPrint(data));
			}
			itextcomponent.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, itextcomponent.getString())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.copy.click"))).withInsertion(itextcomponent.getString()));
			minecraft.player.displayClientMessage(itextcomponent, false);
			minecraft.options.renderDebug = !minecraft.options.renderDebug;
		}

		if (KiwiClientConfig.nbtTooltip && Screen.hasShiftDown() && stack.hasTag()) {
			tooltip.removeIf(c -> c.getClass() == TranslatableComponent.class && "item.nbt_tags".equals(((TranslatableComponent) c).getKey()));
			if (lastNBT != stack.getTag()) {
				switch (KiwiClientConfig.debugTooltipNBTFormatter) {
				case "kiwi":
					formatter = tag -> {
						ChatFormatting[] colors = { ChatFormatting.LIGHT_PURPLE, ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW, ChatFormatting.GREEN, ChatFormatting.AQUA };
						String s = tag.toString();
						StringBuilder sb = new StringBuilder();
						int i = 0;
						boolean quoted = false;
						for (int ch : s.chars().boxed().collect(Collectors.toList())) {
							boolean special = false;
							if (quoted) {
								if (ch == '"') {
									quoted = false;
									sb.appendCodePoint(ch);
									sb.append(ChatFormatting.WHITE);
									continue;
								}
							} else {
								if (ch == ':' || ch == ',') {
									sb.append(ChatFormatting.GRAY);
									sb.appendCodePoint(ch);
									sb.append(ChatFormatting.WHITE);
									continue;
								} else if (ch == '"') {
									quoted = true;
									sb.append(ChatFormatting.GRAY);
								} else if (ch == '{' || ch == '[') {
									++i;
									special = true;
								} else if (ch == '}' || ch == ']') {
									special = true;
								}
							}
							if (special) {
								int colotIndex = i % colors.length;
								sb.append(colors[colotIndex]);
							}
							sb.appendCodePoint(ch);
							if (special) {
								sb.append(ChatFormatting.WHITE);
								if (ch == '}' || ch == ']') {
									--i;
								}
							}
						}
						return new TextComponent(sb.toString());
					};
					break;
				case "vanilla":
					formatter = tag -> NbtUtils.toPrettyComponent(stack.getTag());
					break;
				default:
					formatter = tag -> new TextComponent(tag.toString());
					break;
				}

				lastNBT = stack.getTag();
				lastFormatted = formatter.apply(lastNBT);
			}
			tooltip.add(lastFormatted);
		} else if (KiwiClientConfig.tagsTooltip) {
			stack.getItem().getTags().stream().map(Object::toString).sorted().forEach(id -> {
				tooltip.add(new TextComponent("#" + id).withStyle(ChatFormatting.DARK_GRAY));
			});
		}
	}
}

package snownee.kiwi.client;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.item.ModItem;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(Dist.CLIENT)
public final class TooltipEvents {
	private TooltipEvents() {
	}

	private static ItemStack lastStack;
	private static CompoundNBT lastNBT;
	private static ITextComponent lastFormatted;
	private static Function<CompoundNBT, ITextComponent> formatter;

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void globalTooltip(ItemTooltipEvent event) {
		if (KiwiClientConfig.globalTooltip)
			ModItem.addTip(event.getItemStack(), event.getToolTip(), event.getFlags());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void debugTooltip(ItemTooltipEvent event) {
		if (!Kiwi.areTagsUpdated() || !event.getFlags().isAdvanced()) {
			return;
		}

		ItemStack stack = event.getItemStack();
		Minecraft minecraft = Minecraft.getInstance();
		if (stack != lastStack && minecraft.player != null && InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 292/*F3*/)) {
			lastStack = stack;
			CompoundNBT data = stack.getTag();
			IFormattableTextComponent itextcomponent = new StringTextComponent(stack.getItem().getRegistryName().toString());
			if (minecraft.keyboardListener != null) {
				minecraft.keyboardListener.setClipboardString(itextcomponent.getString());
			}
			if (data != null) {
				itextcomponent.append(data.toFormattedComponent());
			}
			itextcomponent.modifyStyle(style -> {
				return style.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, itextcomponent.getString())).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.copy.click"))).setInsertion(itextcomponent.getString());
			});
			minecraft.player.sendMessage(itextcomponent, Util.DUMMY_UUID);
			minecraft.gameSettings.showDebugInfo = !minecraft.gameSettings.showDebugInfo;
		}

		List<ITextComponent> tooltip = event.getToolTip();

		if (KiwiClientConfig.nbtTooltip && Screen.hasShiftDown() && stack.hasTag()) {
			tooltip.removeIf(c -> c.getClass() == TranslationTextComponent.class && ((TranslationTextComponent) c).getKey().equals("item.nbt_tags"));
			if (lastNBT != stack.getTag()) {
				switch (KiwiClientConfig.debugTooltipNBTFormatter) {
				case "kiwi":
					formatter = tag -> {
						TextFormatting[] colors = { TextFormatting.LIGHT_PURPLE, TextFormatting.RED, TextFormatting.GOLD, TextFormatting.YELLOW, TextFormatting.GREEN, TextFormatting.AQUA };
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
									sb.append(TextFormatting.WHITE);
									continue;
								}
							} else {
								if (ch == ':' || ch == ',') {
									sb.append(TextFormatting.GRAY);
									sb.appendCodePoint(ch);
									sb.append(TextFormatting.WHITE);
									continue;
								} else if (ch == '"') {
									quoted = true;
									sb.append(TextFormatting.GRAY);
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
								sb.append(TextFormatting.WHITE);
								if (ch == '}' || ch == ']') {
									--i;
								}
							}
						}
						return new StringTextComponent(sb.toString());
					};
					break;
				case "vanilla":
					formatter = tag -> stack.getTag().toFormattedComponent();
					break;
				default:
					formatter = tag -> new StringTextComponent(tag.toString());
					break;
				}

				lastNBT = stack.getTag();
				lastFormatted = formatter.apply(lastNBT).deepCopy().mergeStyle(TextFormatting.RESET);
			}
			tooltip.add(lastFormatted);
		} else if (KiwiClientConfig.tagsTooltip) {
			stack.getItem().getTags().stream().map(Object::toString).sorted().forEach(id -> {
				tooltip.add(new StringTextComponent("#" + id).mergeStyle(TextFormatting.DARK_GRAY));
			});
		}
	}
}

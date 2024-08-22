package snownee.kiwi.client;

import java.util.Comparator;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.item.ModItem;
import snownee.kiwi.loader.Platform;

public final class TooltipEvents {
	public static final String disableDebugTooltipCommand = "@kiwi disable debugTooltip";
	private static final DebugTooltipCache cache = new DebugTooltipCache();
	private static boolean firstSeenDebugTooltip = true;
	private static long latestPressF3;
	private static boolean holdAlt;
	private static long holdAltStart;
	private static boolean showTagsBeforeAlt;

	private TooltipEvents() {
	}

	public static void globalTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
		if (KiwiClientConfig.globalTooltip) {
			ModItem.addTip(stack, tooltip, flag);
		}
	}

	public static void debugTooltip(ItemStack itemStack, List<Component> tooltip, TooltipFlag flag) {
		if (!flag.isAdvanced()) {
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		long millis = Util.getMillis();
		if (mc.player != null && millis - latestPressF3 > 500 && InputConstants.isKeyDown(
				Minecraft.getInstance().getWindow().getWindow(),
				InputConstants.KEY_F3)) {
			latestPressF3 = millis;
			MutableComponent component = Component.literal(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());
			mc.keyboardHandler.setClipboard(component.getString());
			component.withStyle(style -> style.withClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, component.getString()))
					.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")))
					.withInsertion(component.getString()));
			mc.player.displayClientMessage(component, false);
			mc.gui.getDebugOverlay().toggleOverlay();
		}

		if (KiwiClientConfig.hideDataComponentsTooltip) {
			tooltip.removeIf(c -> c.getContents() instanceof TranslatableContents &&
					"item.components".equals(((TranslatableContents) c.getContents()).getKey()));
		}
		if (KiwiClientConfig.tagsTooltip) {
			cache.maybeUpdateTags(itemStack);
			boolean alt = Screen.hasAltDown();
			if (!holdAlt && alt) {
				holdAltStart = millis;
				showTagsBeforeAlt = cache.showTags;
			} else if (holdAlt && !alt) {
				if (cache.showTags && millis - holdAltStart < 500) {
					cache.pageNow += Screen.hasControlDown() ? -1 : 1;
					cache.needUpdatePreferredType = true;
				}
			}
			if (alt && millis - holdAltStart >= 500) {
				cache.showTags = !showTagsBeforeAlt;
				cache.lastShowTags = millis;
			}
			holdAlt = alt;

			if (!cache.pages.isEmpty()) {
				trySendTipMsg(mc);
				cache.appendTagsTooltip(tooltip);
			}
		}
	}

	private static void trySendTipMsg(Minecraft mc) {
		if (firstSeenDebugTooltip && mc.player != null) {
			firstSeenDebugTooltip = false;
			if (KiwiClientConfig.debugTooltipMsg) {
				MutableComponent clickHere = Component.translatable("tip.kiwi.click_here").withStyle($ -> $.withClickEvent(new ClickEvent(
						Action.COPY_TO_CLIPBOARD,
						disableDebugTooltipCommand)));
				mc.player.sendSystemMessage(Component.translatable("tip.kiwi.debug_tooltip", clickHere.withStyle(ChatFormatting.AQUA)));
				KiwiClientConfig.debugTooltipMsg = false;
				KiwiConfigManager.getHandler(KiwiClientConfig.class).save();
			}
		}
	}

	private static class DebugTooltipCache {
		private final List<String> pageTypes = Lists.newArrayList();
		private final List<List<String>> pages = Lists.newArrayList();
		private final List<List<String>> translatedPages = Lists.newArrayList();
		private int pageNow = 0;
		private ItemStack itemStack = ItemStack.EMPTY;
		private boolean showTags;
		private long lastShowTags;
		private String preferredType;
		public boolean needUpdatePreferredType;

		public void maybeUpdateTags(ItemStack itemStack) {
			if (this.itemStack == itemStack) {
				return;
			}
			this.itemStack = itemStack;
			pages.clear();
			translatedPages.clear();
			pageTypes.clear();
			pageNow = 0;
			addPages("item", itemStack.getTags());
			Item item = itemStack.getItem();
			Block block = Block.byItem(item);
			if (block != Blocks.AIR) {
				addPages("block", getTags(BuiltInRegistries.BLOCK, block));
			}
			if (item instanceof SpawnEggItem spawnEggItem) {
				EntityType<?> type = spawnEggItem.getType(itemStack);
				addPages("entity_type", getTags(BuiltInRegistries.ENTITY_TYPE, type));
			} else if (item instanceof BucketItem bucketItem) {
				addPages("fluid", getTags(BuiltInRegistries.FLUID, Platform.getFluidFromBucket(bucketItem)));
			}
			for (int i = 0; i < pages.size(); i++) {
				if (pageTypes.get(i).equals(preferredType)) {
					pageNow = i;
					break;
				}
			}
		}

		private static <T> Stream<TagKey<T>> getTags(Registry<T> registry, T object) {
			return registry.getResourceKey(object)
					.flatMap(registry::getHolder)
					.stream()
					.flatMap(Holder::tags);
		}

		public void addPages(String type, Stream<? extends TagKey<?>> stream) {
			List<? extends TagKey<?>> tags = stream.sorted(Comparator.comparing((TagKey<?> $) -> $.location())).toList();
			if (tags.isEmpty()) {
				return;
			}
			int i = 0;
			List<String> page = Lists.newArrayList();
			List<String> translatedPage = Lists.newArrayList();
			for (TagKey<?> tag : tags) {
				page.add("#" + tag.location());
				String translationKey = Platform.getTagTranslationKey(tag);
				if (I18n.exists(translationKey)) {
					translatedPage.add("#" + I18n.get(translationKey));
				} else {
					translatedPage.add("#" + tag.location());
				}
				if (++i == KiwiClientConfig.tagsTooltipTagsPerPage) {
					pages.add(page);
					translatedPages.add(translatedPage);
					pageTypes.add(type);
					page = Lists.newArrayList();
					translatedPage = Lists.newArrayList();
					i = 0;
				}
			}
			if (!page.isEmpty()) {
				pages.add(page);
				translatedPages.add(translatedPage);
				pageTypes.add(type);
			}
		}

		public void appendTagsTooltip(List<Component> tooltip) {
			if (pages.isEmpty()) {
				return;
			}
			if (showTags && Util.getMillis() - lastShowTags > 60000) {
				showTags = false;
			}
			if (!showTags) {
				if (KiwiClientConfig.tagsTooltipAppendKeybindHint) {
					findIdLine(tooltip, i -> tooltip.set(i, tooltip.get(i).copy().append(" (alt)")));
				}
				return;
			}
			lastShowTags = Util.getMillis();
			List<Component> sub = Lists.newArrayList();
			pageNow = Math.floorMod(pageNow, pages.size());
			if (needUpdatePreferredType) {
				needUpdatePreferredType = false;
				preferredType = pageTypes.get(pageNow);
			}
			boolean showTranslatedTags = KiwiClientConfig.showTranslatedTagsByDefault ^ Screen.hasControlDown();
			List<String> page = showTranslatedTags ? translatedPages.get(pageNow) : pages.get(pageNow);
			for (String tag : page) {
				sub.add(Component.literal(tag).withStyle(ChatFormatting.DARK_GRAY));
			}
			int index = findIdLine(tooltip, i -> {
				String type = pageTypes.get(pageNow);
				tooltip.set(i, tooltip.get(i).copy().append(" (%s/%s...%s)".formatted(pageNow + 1, pages.size(), type)));
			});
			index = index == -1 ? tooltip.size() : index + 1;
			tooltip.addAll(index, sub);
		}

		private int findIdLine(List<Component> tooltip, IntConsumer consumer) {
			String id = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
			for (int i = 0; i < tooltip.size(); i++) {
				Component component = tooltip.get(i);
				if (component.getString().equals(id)) {
					consumer.accept(i);
					return i;
				}
			}
			return -1;
		}
	}
}

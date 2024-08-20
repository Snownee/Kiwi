package snownee.kiwi;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import snownee.kiwi.KiwiModule.Category;
import snownee.kiwi.item.ItemCategoryFiller;

public class GroupSetting {

	public static GroupSetting of(Category category, GroupSetting preset) {
		if (preset != null) {
			if (category.value().length == 0 && category.after().length == 0) {
				return preset;
			}
			if (category.value().length == 0) {
				return new GroupSetting(preset.groups, category.after());
			}
		}
		return new GroupSetting(category.value(), category.after());
	}

	private final String[] groups;
	private final String[] after;
	private final List<ItemCategoryFiller> fillers = Lists.newArrayList();

	public GroupSetting(String[] groups, String[] after) {
		this.groups = groups;
		this.after = after;
	}

	public void apply(ItemCategoryFiller filler) {
		fillers.add(filler);
	}

	public void postApply() {
		List<ResourceKey<CreativeModeTab>> tabKeys = Stream.of(groups)
				.map($ -> {
					ResourceKey<CreativeModeTab> tab = Kiwi.getGroup($);
					if (tab != null) {
						return tab;
					}
					return ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.parse($));
				})
				.toList();
		IEventBus eventBus = Objects.requireNonNull(ModContext.get(Kiwi.ID).modContainer.getEventBus());
		for (ResourceKey<CreativeModeTab> tabKey : tabKeys) {
			eventBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
				if (!event.getTabKey().equals(tabKey)) {
					return;
				}
				List<Item> afterItems = Stream.of(after)
						.map(ResourceLocation::tryParse)
						.filter(Objects::nonNull)
						.map(BuiltInRegistries.ITEM::get)
						.filter(Predicate.not(Items.AIR::equals))
						.toList();
				List<ItemStack> items = Lists.newArrayList();
				for (ItemCategoryFiller filler : fillers) {
					CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(tabKey);
					filler.fillItemCategory(tab, event.getFlags(), event.hasPermissions(), items);
				}
				items = getEnabledStacks(items, event.getFlags());
				addAfter(items, event, afterItems);
			});
		}
	}

	//TODO test it
	private static void addAfter(
			List<ItemStack> toAdd,
			BuildCreativeModeTabContentsEvent event,
			Collection<Item> afterItems) {
		ObjectSortedSet<ItemStack> parentEntries = event.getParentEntries();
		ItemStack lastFound = ItemStack.EMPTY;
		for (Item item : afterItems) {
			ItemStack stack = new ItemStack(item);
			if (parentEntries.contains(stack)) {
				lastFound = stack;
			}
		}
		ItemStack prev = ItemStack.EMPTY;
		for (int i = 0; i < toAdd.size(); i++) {
			ItemStack item = toAdd.get(i);
			if (i == 0) {
				if (lastFound.isEmpty()) {
					event.accept(item, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
				} else {
					event.insertAfter(lastFound, item, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
				}
			} else {
				event.insertAfter(prev, item, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			}
			prev = item;
		}
	}

	private static List<ItemStack> getEnabledStacks(List<ItemStack> newStacks, FeatureFlagSet enabledFeatures) {
		// If not all stacks are enabled, filter the list, otherwise use it as-is
		if (newStacks.stream().allMatch($ -> isEnabled($, enabledFeatures))) {
			return newStacks;
		}

		return newStacks.stream().filter($ -> isEnabled($, enabledFeatures)).toList();
	}

	private static boolean isEnabled(ItemStack stack, FeatureFlagSet enabledFeatures) {
		return stack.getItem().isEnabled(enabledFeatures);
	}
}

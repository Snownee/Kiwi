package snownee.kiwi;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import snownee.kiwi.KiwiModule.Category;
import snownee.kiwi.item.ItemCategoryFiller;

public class GroupSetting {

	public static GroupSetting of(Category category, GroupSetting preset) {
		if (preset != null && category.value().length == 0) {
			return new GroupSetting(preset.groups, category.after());
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
					CreativeModeTab tab = Kiwi.getGroup($);
					if (tab != null) {
						return BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).orElse(null);
					}
					return ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation($));
				})
				.filter(Objects::nonNull)
				.toList();
		for (ResourceKey<CreativeModeTab> tabKey : tabKeys) {
			ItemGroupEvents.modifyEntriesEvent(tabKey).register(entries -> {
				Set<Item> afterItems = Stream.of(after)
						.map(ResourceLocation::tryParse)
						.filter(Objects::nonNull)
						.map(BuiltInRegistries.ITEM::get)
						.filter(Predicate.not(Items.AIR::equals))
						.collect(Collectors.toSet());
				List<ItemStack> items = Lists.newArrayList();
				for (ItemCategoryFiller filler : fillers) {
					CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(tabKey);
					filler.fillItemCategory(tab, entries.getEnabledFeatures(), entries.shouldShowOpRestrictedItems(), items);
				}
				items = getEnabledStacks(items, entries.getEnabledFeatures());
				addAfter(items, entries.getDisplayStacks(), afterItems);
				addAfter(items, entries.getSearchTabStacks(), afterItems);
			});
		}
	}

	private static void addAfter(List<ItemStack> toAdd, List<ItemStack> destination, Set<Item> afterItems) {
		int lastFound = -1;
		if (!afterItems.isEmpty()) {
			for (int i = 0; i < destination.size(); i++) {
				ItemStack stack = destination.get(i);
				if (afterItems.contains(stack.getItem())) {
					lastFound = i;
				}
			}
		}
		if (lastFound >= 0) {
			destination.addAll(lastFound + 1, toAdd);
		} else {
			destination.addAll(toAdd);
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

	public boolean isEmpty() {
		return groups.length == 0 && after.length == 0;
	}
}

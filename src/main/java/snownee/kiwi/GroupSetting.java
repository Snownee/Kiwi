package snownee.kiwi;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.MutableHashedLinkedMap;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import snownee.kiwi.KiwiModule.Category;
import snownee.kiwi.item.ItemCategoryFiller;

public class GroupSetting {

	public static GroupSetting of(Category category, GroupSetting preset) {
		if (preset != null && category.value().length == 0) {
			return new GroupSetting(preset.groups, category.after());
		}
		return new GroupSetting(category.value(), category.after());
	}

	private static final Multimap<CreativeModeTab, Pair<List<ItemStack>, ItemCategoryFiller>> toAdd = HashMultimap.create();

	static {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(GroupSetting::buildContents);
	}

	private String[] groups;
	private String[] after;

	public GroupSetting(String[] groups, String[] after) {
		this.groups = groups;
		this.after = after;
	}

	@SuppressWarnings("deprecation")
	public void apply(ItemCategoryFiller filler) {
		/* off */
		List<CreativeModeTab> tabs = Stream.of(groups)
				.map(Kiwi::getGroup)
				.filter(Objects::nonNull)
				.toList();
		List<ItemStack> afterItems = Stream.of(after)
				.map(ResourceLocation::tryParse)
				.filter(Objects::nonNull)
				.map(BuiltInRegistries.ITEM::get)
				.filter(Predicate.not(Items.AIR::equals))
				.map(ItemStack::new)
				.toList();
		/* on */
		for (CreativeModeTab tab : tabs) {
			toAdd.put(tab, Pair.of(afterItems, filler));
		}
	}

	private static void addAfter(List<ItemStack> toAdd, MutableHashedLinkedMap<ItemStack, TabVisibility> map, List<ItemStack> afterItems) {
		ItemStack lastFound = ItemStack.EMPTY;
		if (!afterItems.isEmpty()) {
			for (int i = 0; i < afterItems.size(); i++) {
				ItemStack stack = afterItems.get(afterItems.size() - i - 1);
				if (map.contains(stack)) {
					lastFound = stack;
					break;
				}
			}
		}
		for (ItemStack item : toAdd) {
			if (lastFound.isEmpty()) {
				map.put(item, TabVisibility.PARENT_AND_SEARCH_TABS);
			} else {
				map.putAfter(lastFound, item, TabVisibility.PARENT_AND_SEARCH_TABS);
			}
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

	private static void buildContents(BuildCreativeModeTabContentsEvent event) {
		for (var pair : toAdd.get(event.getTab())) {
			List<ItemStack> items = Lists.newArrayList();
			pair.getRight().fillItemCategory(event.getTab(), event.getFlags(), event.hasPermissions(), items);
			items = getEnabledStacks(items, event.getFlags());
			if (items.isEmpty()) {
				continue;
			}
			addAfter(items, event.getEntries(), pair.getLeft());
		}
	}
}

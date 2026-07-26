package snownee.kiwi.customization.block.family;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.neoforged.neoforge.common.Tags;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiCommonConfig;
import snownee.kiwi.customization.CustomizationHooks;
import snownee.kiwi.util.KHolder;
import snownee.kiwi.util.resource.OneTimeLoader;

public class BlockFamilies {
	public static final long BASE_MAT_VALUE = 81000;
	private static ImmutableListMultimap<Item, KHolder<BlockFamily>> byItem = ImmutableListMultimap.of();
	private static ImmutableList<KHolder<BlockFamily>> fromResources = ImmutableList.of();
	private static ImmutableMap<Identifier, KHolder<BlockFamily>> byId = ImmutableMap.of();
	private static ImmutableListMultimap<Item, KHolder<BlockFamily>> byStonecutterSource = ImmutableListMultimap.of();
	private static Map<String, BlockFamilyInferrer.AddonRule> addonRules = Map.of();

	public static Collection<KHolder<BlockFamily>> find(Item item) {
		if (item == Items.AIR) {
			return List.of();
		}
		return byItem.get(item);
	}

	public static List<KHolder<BlockFamily>> findQuickSwitch(Item item, boolean hasInfiniteMaterials) {
		Stream<KHolder<BlockFamily>> stream = find(item).stream();
		if (hasInfiniteMaterials) {
			stream = stream.filter(f -> f.value().switchAttrs().enabled());
		} else {
			stream = stream.filter(f -> f.value().switchAttrs().enabled() && !f.value().switchAttrs().creativeOnly());
		}
		return stream.toList();
	}

	public static Collection<KHolder<BlockFamily>> findByStonecutterSource(Item item) {
		return byStonecutterSource.get(item);
	}

	public static void reloadResources(ResourceManager resourceManager, OneTimeLoader.Context context) {
		Map<Identifier, BlockFamily> families = OneTimeLoader.load(resourceManager, "kiwi/family", BlockFamily.DIRECT_CODEC, context);
		fromResources = families.entrySet()
				.stream()
				.map(e -> new KHolder<>(e.getKey(), e.getValue()))
				.collect(ImmutableList.toImmutableList());
		// we need the byItem cache for automatically generating families
		// we also need the byId cache because it is referenced by BuilderRules
		addonRules = BlockFamilyInferrer.loadAddonRules(resourceManager, context);
		reloadComplete(List::of);
	}

	public static int reloadRecipes(Collection<RecipeHolder<StonecutterRecipe>> recipes) {
		if (CustomizationHooks.kswitch) {
			reloadComplete(new BlockFamilyInferrer(
					addonRules,
					KiwiCommonConfig.kSwitchAutoStonecuttingRecipes ? recipes : List.of())::generate);
		}
		return byId.size();
	}

	private static void reloadComplete(Supplier<Collection<KHolder<BlockFamily>>> additionalSupplier) {
		byId = ImmutableMap.of();
		byItem = ImmutableListMultimap.of();
		byStonecutterSource = ImmutableListMultimap.of();
		Collection<KHolder<BlockFamily>> additional = additionalSupplier.get();
		Map<Identifier, KHolder<BlockFamily>> byIdBuilder = Maps.newHashMapWithExpectedSize(fromResources.size() + additional.size());
		ImmutableListMultimap.Builder<Item, KHolder<BlockFamily>> byItemBuilder = ImmutableListMultimap.builder();
		ImmutableListMultimap.Builder<Item, KHolder<BlockFamily>> byStonecutterBuilder = ImmutableListMultimap.builder();
		for (var family : Iterables.concat(fromResources, additional)) {
			KHolder<BlockFamily> old = byIdBuilder.putIfAbsent(family.key(), family);
			if (old != null) {
				Kiwi.LOGGER.error("Duplicate family {}", family);
				continue;
			}
			for (var item : family.value().itemHolders()) {
				byItemBuilder.put(item.value(), family);
			}
			Optional<Holder.Reference<Item>> stonecutterSource = family.value().stonecutterFrom();
			//noinspection OptionalIsPresent
			if (stonecutterSource.isPresent()) {
				byStonecutterBuilder.put(stonecutterSource.get().value(), family);
			}
		}
		byId = ImmutableMap.copyOf(byIdBuilder);
		byItem = byItemBuilder.build();
		byStonecutterSource = byStonecutterBuilder.build();
	}

	@Nullable
	public static BlockFamily get(Identifier id) {
		KHolder<BlockFamily> holder = byId.get(id);
		return holder == null ? null : holder.value();
	}

	public static Collection<KHolder<BlockFamily>> all() {
		return byId.values();
	}

	public static long getMatValue(ItemInstance item) {
		return getMatValue(item.typeHolder()) * item.count();
	}

	public static long getMatValue(Item item) {
		return getMatValue(BuiltInRegistries.ITEM.wrapAsHolder(item));
	}

	private static long getMatValue(Holder<Item> holder) {
		if (holder.is(Tags.Items.STORAGE_BLOCKS)) {
			return BASE_MAT_VALUE * 9;
		}
		if (holder.is(ItemTags.SLABS)) {
			return BASE_MAT_VALUE / 2;
		}
		if (holder.is(ItemTags.DOORS)) {
			return BASE_MAT_VALUE * 2;
		}
		if (holder.is(ItemTags.TRAPDOORS)) {
			return BASE_MAT_VALUE * 3;
		}
		if (holder.is(ItemTags.FENCE_GATES)) {
			return BASE_MAT_VALUE * 4;
		}
		if (holder.is(ItemTags.WOODEN_PRESSURE_PLATES)) {
			return BASE_MAT_VALUE * 2;
		}
		if (holder.is(ItemTags.BARS)) {
			return BASE_MAT_VALUE / 24;
		}
		return BASE_MAT_VALUE;
	}

	@Nullable
	public static Identifier getKey(BlockFamily family) {
		for (KHolder<BlockFamily> holder : all()) {
			if (holder.value() == family) {
				return holder.key();
			}
		}
		return null;
	}
}
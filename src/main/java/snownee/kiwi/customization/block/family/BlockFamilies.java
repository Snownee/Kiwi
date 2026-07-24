package snownee.kiwi.customization.block.family;

import java.util.ArrayList;
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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
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
	private static List<MatValueEntry> matValueEntries = List.of();

	record MatValueEntry(TagKey<Item> tag, int multiply, int divide) {
		static final Codec<MatValueEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(MatValueEntry::tag),
				ExtraCodecs.POSITIVE_INT.optionalFieldOf("multiply", 1).forGetter(MatValueEntry::multiply),
				ExtraCodecs.POSITIVE_INT.optionalFieldOf("divide", 1).forGetter(MatValueEntry::divide)
		).apply(instance, MatValueEntry::new));
	}

	record MatValueFile(int priority, List<MatValueEntry> entries) {
		static final Codec<MatValueFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.optionalFieldOf("priority", 0).forGetter(MatValueFile::priority),
				MatValueEntry.CODEC.listOf().fieldOf("entries").forGetter(MatValueFile::entries)
		).apply(instance, MatValueFile::new));
	}

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
		Map<Identifier, MatValueFile> matValueFiles = OneTimeLoader.load(resourceManager, "kiwi/mat_value", MatValueFile.CODEC, context);
		List<MatValueEntry> allEntries = new ArrayList<>();
		matValueFiles.values().stream()
				.sorted((a, b) -> Integer.compare(b.priority(), a.priority()))
				.forEach(file -> allEntries.addAll(file.entries()));
		matValueEntries = List.copyOf(allEntries);
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
		for (MatValueEntry entry : matValueEntries) {
			if (holder.is(entry.tag())) {
				long value = BASE_MAT_VALUE;
				if (entry.multiply() != 1) {
					value *= entry.multiply();
				}
				if (entry.divide() != 1) {
					value /= entry.divide();
				}
				return value;
			}
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
package snownee.kiwi.customization.block.family;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import snownee.kiwi.util.KHolder;
import snownee.kiwi.util.resource.OneTimeLoader;

public class BlockFamilies {
	private static ImmutableListMultimap<Item, KHolder<BlockFamily>> byItem = ImmutableListMultimap.of();
	private static ImmutableMap<ResourceLocation, KHolder<BlockFamily>> byId = ImmutableMap.of();
	private static ImmutableListMultimap<Item, KHolder<BlockFamily>> byStonecutterSource = ImmutableListMultimap.of();

	public static Collection<KHolder<BlockFamily>> find(Item item) {
		if (item == Items.AIR) {
			return List.of();
		}
		return byItem.get(item);
	}

	public static List<KHolder<BlockFamily>> findQuickSwitch(Item item) {
		return find(item).stream().filter(f -> f.value().quickSwitch()).toList();
	}

	public static Collection<KHolder<BlockFamily>> findByStonecutterSource(Item item) {
		return byStonecutterSource.get(item);
	}

	public static int reload(ResourceManager resourceManager) {
		Map<ResourceLocation, BlockFamily> families = OneTimeLoader.load(resourceManager, "kiwi/family", BlockFamily.CODEC);
		byId = ImmutableMap.copyOf(families.entrySet()
				.stream()
				.map(e -> new KHolder<>(e.getKey(), e.getValue()))
				.collect(ImmutableMap.toImmutableMap(
						KHolder::key,
						Function.identity())));
		ImmutableListMultimap.Builder<Item, KHolder<BlockFamily>> byItemBuilder = ImmutableListMultimap.builder();
		ImmutableListMultimap.Builder<Item, KHolder<BlockFamily>> byStonecutterBuilder = ImmutableListMultimap.builder();
		for (var family : byId.values()) {
			for (var item : family.value().itemHolders()) {
				byItemBuilder.put(item.value(), family);
			}
			Item stonecutterFrom = family.value().stonecutterSource();
			if (stonecutterFrom != Items.AIR) {
				byStonecutterBuilder.put(stonecutterFrom, family);
			}
		}
		byItem = byItemBuilder.build();
		byStonecutterSource = byStonecutterBuilder.build();
		StonecutterRecipeMaker.invalidateCache();
		return byId.size();
	}

	public static BlockFamily get(ResourceLocation id) {
		KHolder<BlockFamily> holder = byId.get(id);
		return holder == null ? null : holder.value();
	}

	public static Collection<KHolder<BlockFamily>> all() {
		return byId.values();
	}
}

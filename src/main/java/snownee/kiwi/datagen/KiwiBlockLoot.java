package snownee.kiwi.datagen;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import snownee.kiwi.KiwiModuleContainer;
import snownee.kiwi.KiwiModules;

public abstract class KiwiBlockLoot extends BlockLootSubProvider {
	protected final Identifier moduleId;
	private final List<Block> knownBlocks;
	private final Map<Class<?>, Function<Block, LootTable.Builder>> handlers = Maps.newIdentityHashMap();
	private final Set<Block> added = Sets.newHashSet();
	private Function<Block, LootTable.Builder> defaultHandler;

	protected KiwiBlockLoot(Identifier moduleId, CompletableFuture<HolderLookup.Provider> registryLookup) {
		super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registryLookup.join());
		this.moduleId = moduleId;
		KiwiModuleContainer container = Objects.requireNonNull(KiwiModules.get(moduleId));
		knownBlocks = container.getRegistries(Registries.BLOCK);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Block> void handle(Class<T> clazz, Function<T, LootTable.Builder> handler) {
		handlers.put(clazz, (Function<Block, LootTable.Builder>) handler);
	}

	protected void handleDefault(Function<Block, LootTable.Builder> handler) {
		defaultHandler = handler;
	}

	@Override
	protected void generate() {
		addTables();
		for (Block block : knownBlocks) {
			if (added.contains(block)) {
				continue;
			}
			added.add(block);
			Function<Block, LootTable.Builder> handler = handlers.get(block.getClass());
			if (handler == null) {
				handler = defaultHandler;
			}
			if (handler != null) {
				LootTable.Builder builder = handler.apply(block);
				if (builder != null) {
					add(block, builder);
				}
			}
		}
	}

	protected abstract void addTables();

	@Override
	protected void add(Block block, LootTable.Builder builder) {
		super.add(block, builder);
		added.add(block);
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return knownBlocks;
	}
}
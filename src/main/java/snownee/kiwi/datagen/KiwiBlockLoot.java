package snownee.kiwi.datagen;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import snownee.kiwi.KiwiModuleContainer;
import snownee.kiwi.KiwiModules;

public abstract class KiwiBlockLoot extends FabricBlockLootTableProvider {
	protected final ResourceLocation moduleId;
	private final List<Block> knownBlocks;
	private final Map<Class<?>, Function<Block, LootTable.Builder>> handlers = Maps.newIdentityHashMap();
	private final Set<Block> added = Sets.newHashSet();
	private Function<Block, LootTable.Builder> defaultHandler;

	protected KiwiBlockLoot(ResourceLocation moduleId, FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
		super(dataOutput, registryLookup);
		this.moduleId = moduleId;
		KiwiModuleContainer container = Objects.requireNonNull(KiwiModules.get(moduleId));
		knownBlocks = container.getRegistries(Registries.BLOCK);
	}

	protected <T extends Block> void handle(Class<T> clazz, Function<T, LootTable.Builder> handler) {
		handlers.put(clazz, (Function<Block, Builder>) handler);
	}

	protected void handleDefault(Function<Block, LootTable.Builder> handler) {
		defaultHandler = handler;
	}

	@Override
	public void generate() {
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
	public void add(Block block, LootTable.Builder builder) {
		super.add(block, builder);
		added.add(block);
	}

	public List<Block> getKnownBlocks() {
		return knownBlocks;
	}

	@Override
	public String getName() {
		return super.getName() + " - " + moduleId;
	}
}

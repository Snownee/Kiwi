package snownee.kiwi.data.provider;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.data.loot.BlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import snownee.kiwi.KiwiManager;
import snownee.kiwi.ModuleInfo;

public abstract class KiwiBlockLoot extends BlockLoot {

	public List<Block> knownBlocks;
	private final Map<Class<?>, Function<Block, LootTable.Builder>> handlers = Maps.newHashMap();
	private Function<Block, LootTable.Builder> defaultHandler;
	private final Set<Block> added = Sets.newHashSet();

	public KiwiBlockLoot(ResourceLocation moduleId) {
		ModuleInfo info = KiwiManager.MODULES.get(moduleId);
		if (info != null) {
			knownBlocks = info.getRegistries(Block.class);
		}
	}

	protected void handle(Class<?> clazz, Function<Block, LootTable.Builder> handler) {
		handlers.put(clazz, handler);
	}

	protected void handleDefault(Function<Block, LootTable.Builder> handler) {
		defaultHandler = handler;
	}

	@Override
	protected final void addTables() {
		_addTables();
		for (Block block : getKnownBlocks()) {
			if (added.contains(block)) {
				continue;
			}
			Function<Block, Builder> handler = handlers.get(block.getClass());
			if (handler != null) {
				add(block, handler);
			} else if (defaultHandler != null) {
				add(block, defaultHandler);
			}
		}
	}

	protected void add(Block block, LootTable.Builder builder) {
		super.add(block, builder);
		added.add(block);
	}

	protected abstract void _addTables();

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return knownBlocks;
	}

}

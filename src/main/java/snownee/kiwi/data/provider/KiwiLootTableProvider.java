/*
package snownee.kiwi.data.provider;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class KiwiLootTableProvider extends LootTableProvider {

	private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> subProviders = Lists.newArrayList();

	public KiwiLootTableProvider(DataGenerator pGenerator) {
		super(pGenerator);
	}

	@Override
	public String getName() {
		return super.getName() + " - " + getClass().getSimpleName();
	}

	@Override
	protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootContextParamSet>> getTables() {
		return subProviders;
	}

	public KiwiLootTableProvider add(Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>> consumer, LootContextParamSet lootContextParamSet) {
		subProviders.add(Pair.of(consumer, lootContextParamSet));
		return this;
	}

	@Override
	protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
		map.forEach((p_218436_2_, p_218436_3_) -> {
			LootTables.validate(validationtracker, p_218436_2_, p_218436_3_);
		});
	}

}
*/
package snownee.kiwi.datagen.provider;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.ResourceLocation;

public class KiwiLootTableProvider extends LootTableProvider {

	private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> subProviders = Lists.newArrayList();

	public KiwiLootTableProvider(DataGenerator pGenerator) {
		super(pGenerator);
	}

	@Override
	public String getName() {
		return super.getName() + " - " + getClass().getSimpleName();
	}

	@Override
	protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
		return subProviders;
	}

	public KiwiLootTableProvider add(Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>> consumer, LootParameterSet lootContextParamSet) {
		subProviders.add(Pair.of(consumer, lootContextParamSet));
		return this;
	}

	@Override
	protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
		map.forEach((p_218436_2_, p_218436_3_) -> {
			LootTableManager.validateLootTable(validationtracker, p_218436_2_, p_218436_3_);
		});
	}
}

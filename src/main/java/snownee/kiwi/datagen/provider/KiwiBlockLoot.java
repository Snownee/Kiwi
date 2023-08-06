package snownee.kiwi.datagen.provider;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.loot.CanToolPerformAction;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.KiwiModules;
import snownee.kiwi.ModuleInfo;

@Deprecated
public abstract class KiwiBlockLoot extends BlockLootSubProvider {
	public static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));
	public static final LootItemCondition.Builder HAS_NO_SILK_TOUCH = HAS_SILK_TOUCH.invert();
	public static final LootItemCondition.Builder HAS_SHEARS = CanToolPerformAction.canToolPerformAction(ToolActions.SHEARS_DIG);
	public static final LootItemCondition.Builder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
	public static final LootItemCondition.Builder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();

	public List<Block> knownBlocks;
	private final Map<Class<?>, Function<Block, LootTable.Builder>> handlers = Maps.newHashMap();
	private Function<Block, LootTable.Builder> defaultHandler;
	private final Set<Block> added = Sets.newHashSet();

	public KiwiBlockLoot(ResourceLocation moduleId, Set<Item> explosionResistant, FeatureFlagSet enabledFeatures) {
		super(explosionResistant, enabledFeatures);
		ModuleInfo info = KiwiModules.get(moduleId);
		Objects.requireNonNull(info);
		knownBlocks = info.getRegistries(ForgeRegistries.BLOCKS);
	}

	protected void handle(Class<? extends Block> clazz, Function<Block, LootTable.Builder> handler) {
		handlers.put(clazz, handler);
	}

	protected void handleDefault(Function<Block, LootTable.Builder> handler) {
		defaultHandler = handler;
	}

	@Override
	protected final void generate() {
		addTables();
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

	@Override
	protected void add(Block block, LootTable.Builder builder) {
		super.add(block, builder);
		added.add(block);
	}

	protected abstract void addTables();

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return knownBlocks;
	}

	public static LootTable.Builder createSelfDropDispatchTable(Block p_252253_, LootItemCondition.Builder p_248764_, LootPoolEntryContainer.Builder<?> p_249146_) {
		return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(p_252253_).when(p_248764_).otherwise(p_249146_)));
	}

	public static LootTable.Builder createSilkTouchDispatchTable(Block p_250203_, LootPoolEntryContainer.Builder<?> p_252089_) {
		return createSelfDropDispatchTable(p_250203_, HAS_SILK_TOUCH, p_252089_);
	}

	public static LootTable.Builder createShearsDispatchTable(Block p_252195_, LootPoolEntryContainer.Builder<?> p_250102_) {
		return createSelfDropDispatchTable(p_252195_, HAS_SHEARS, p_250102_);
	}

	public static LootTable.Builder createSilkTouchOrShearsDispatchTable(Block p_250539_, LootPoolEntryContainer.Builder<?> p_251459_) {
		return createSelfDropDispatchTable(p_250539_, HAS_SHEARS_OR_SILK_TOUCH, p_251459_);
	}

	public static LootTable.Builder createSilkTouchOnlyTable(ItemLike p_252216_) {
		return LootTable.lootTable().withPool(LootPool.lootPool().when(HAS_SILK_TOUCH).setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(p_252216_)));
	}

	public static LootTable.Builder createShearsOnlyDrop(ItemLike p_250684_) {
		return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(HAS_SHEARS).add(LootItem.lootTableItem(p_250684_)));
	}
}

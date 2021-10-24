package snownee.kiwi.data.provider;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.loot.CanToolPerformAction;
import snownee.kiwi.KiwiModules;
import snownee.kiwi.ModuleInfo;

public abstract class KiwiBlockLoot extends BlockLoot {
	public static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));
	public static final LootItemCondition.Builder HAS_NO_SILK_TOUCH = HAS_SILK_TOUCH.invert();
	public static final LootItemCondition.Builder HAS_SHEARS = CanToolPerformAction.canToolPerformAction(ToolActions.SHEARS_DIG);
	public static final LootItemCondition.Builder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
	public static final LootItemCondition.Builder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();

	public List<Block> knownBlocks;
	private final Map<Class<?>, Function<Block, LootTable.Builder>> handlers = Maps.newHashMap();
	private Function<Block, LootTable.Builder> defaultHandler;
	private final Set<Block> added = Sets.newHashSet();

	public KiwiBlockLoot(ResourceLocation moduleId) {
		ModuleInfo info = KiwiModules.get(moduleId);
		Preconditions.checkNotNull(info);
		knownBlocks = info.getRegistries(Block.class);
	}

	protected void handle(Class<? extends Block> clazz, Function<Block, LootTable.Builder> handler) {
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

	@Override
	protected void add(Block block, LootTable.Builder builder) {
		super.add(block, builder);
		added.add(block);
	}

	protected abstract void _addTables();

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return knownBlocks;
	}

	public static LootTable.Builder createShearsDispatchTable(Block pBlock, LootPoolEntryContainer.Builder<?> pAlternativeEntryBuilder) {
		return createSelfDropDispatchTable(pBlock, HAS_SHEARS, pAlternativeEntryBuilder);
	}

	public static LootTable.Builder createShearsOnlyDrop(ItemLike p_124287_) {
		return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(HAS_SHEARS).add(LootItem.lootTableItem(p_124287_)));
	}

	public static LootTable.Builder createSilkTouchOrShearsDispatchTable(Block pBlock, LootPoolEntryContainer.Builder<?> pAlternativeEntryBuilder) {
		return createSelfDropDispatchTable(pBlock, HAS_SHEARS_OR_SILK_TOUCH, pAlternativeEntryBuilder);
	}

	public static LootTable.Builder createLeavesDrops(Block pLeavesBlock, Block pSaplingBlock, float... pChances) {
		return createSilkTouchOrShearsDispatchTable(pLeavesBlock, applyExplosionCondition(pLeavesBlock, LootItem.lootTableItem(pSaplingBlock)).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, pChances))).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(HAS_NO_SHEARS_OR_SILK_TOUCH).add(applyExplosionDecay(pLeavesBlock, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F))));
	}
}

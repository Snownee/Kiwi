package snownee.kiwi.test;

import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.effect.HealthBoostMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Categories;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Category;
import snownee.kiwi.KiwiModule.RenderLayer;
import snownee.kiwi.KiwiModule.RenderLayer.Layer;
import snownee.kiwi.block.entity.RetextureBlockEntity;
import snownee.kiwi.item.ModBlockItem;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.kiwi.loader.event.ServerInitEvent;
import snownee.kiwi.schedule.Scheduler;
import snownee.kiwi.util.EnumUtil;

@KiwiModule("test")
@KiwiModule.Optional(defaultEnabled = false)
@KiwiModule.Category(value = Categories.BUILDING_BLOCKS, after = "redstone_block")
@KiwiModule.Subscriber(modBus = true)
public class TestModule extends AbstractModule {
	// Keep your fields `public static`

	// Register a simple item
	@Category(Categories.FOOD_AND_DRINKS)
	public static final KiwiGO<TestItem> FIRST_ITEM = go(() -> new TestItem(itemProp().rarity(Rarity.EPIC)));

	// The next block will use this builder to build its BlockItem. After that this field will be null
	public static Item.Properties FIRST_BLOCK_ITEM_BUILDER = itemProp().rarity(Rarity.RARE);
	// Register a simple block and its BlockItem
	@RenderLayer(Layer.CUTOUT)
	public static final KiwiGO<TestBlock> FIRST_BLOCK = go(() -> new TestBlock2(blockProp(Material.WOOD)));

	// Register a simple effect
	public static final KiwiGO<MobEffect> FIRST_EFFECT = go(() -> new HealthBoostMobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000));

	// And its potion
	public static final KiwiGO<Potion> FIRST_POTION = go(() -> new Potion(new MobEffectInstance(FIRST_EFFECT.get(), 1800)));

	public static final KiwiGO<BlockEntityType<TestBlockEntity>> FIRST_TILE = blockEntity(TestBlockEntity::new, null, FIRST_BLOCK);

	public static final KiwiGO<TestBlock> TEX_BLOCK = go(() -> new TestBlock(blockProp(Material.WOOD)));
	public static final KiwiGO<BlockEntityType<TexBlockEntity>> TEX_TILE = blockEntity(TexBlockEntity::new, null, TEX_BLOCK);

	public static TestModule INSTANCE;

	//	public static RecipeType<CraftingRecipe> RECIPE_TYPE = new RecipeType<>() {};

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void clientInit(ClientInitEvent event) {
		ModBlockItem.INSTANT_UPDATE_TILES.add(FIRST_TILE.get());
		ModBlockItem.INSTANT_UPDATE_TILES.add(TEX_TILE.get());
		ItemBlockRenderTypes.setRenderLayer(TEX_BLOCK.get(), EnumUtil.BLOCK_RENDER_TYPES::contains);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void blockColors(RegisterColorHandlersEvent.Block event) {
		event.register((state, level, pos, i) -> {
			if (level != null && pos != null) {
				BlockEntity blockEntity = level.getBlockEntity(pos);
				if (blockEntity instanceof RetextureBlockEntity) {
					return ((RetextureBlockEntity) blockEntity).getColor(level, i);
				}
			}
			return -1;
		}, TEX_BLOCK.get());
	}

	@Override
	protected void serverInit(ServerInitEvent event) {
		Scheduler.register(MyTask.ID, MyTask.class);
	}

	@Override
	protected void gatherData(GatherDataEvent event) {
		DataGenerator gen = event.getGenerator();
		if (event.includeServer()) {
			gen.addProvider(true, new LootTableProvider(event.getGenerator().getPackOutput(), Set.of(), List.of(new SubProviderEntry(TestBlockLoot::new, LootContextParamSets.BLOCK))));
			gen.addProvider(true, new TestRecipeProvider(event.getGenerator().getPackOutput()));
		}
	}
}

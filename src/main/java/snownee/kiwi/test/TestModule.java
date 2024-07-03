package snownee.kiwi.test;

import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeType;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Categories;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Category;

@KiwiModule("test")
@KiwiModule.Optional(defaultEnabled = false)
@KiwiModule.Category(value = Categories.BUILDING_BLOCKS, after = "redstone_block")
//@KiwiModule.Subscriber(Bus.MOD)
public class TestModule extends AbstractModule {
	// Keep your fields `public static`

	// Register a simple item
	@Category(value = Categories.FOOD_AND_DRINKS, after = "apple")
	public static final KiwiGO<TestItem> FIRST_ITEM = go(() -> new TestItem(itemProp().rarity(Rarity.EPIC)));
	public static final KiwiGO<TestItem> ITEM2 = go(() -> new TestItem(itemProp()));
	@Category(value = Categories.FOOD_AND_DRINKS, after = "kiwi:item2")
	public static final KiwiGO<TestItem> ITEM3 = go(() -> new TestItem(itemProp()));
	public static final KiwiGO<TestItem> ITEM4 = go(() -> new TestItem(itemProp()));

	// The next block will use this builder to build its BlockItem. After that this field will be null
	public static Item.Properties FIRST_BLOCK_ITEM_BUILDER = itemProp().rarity(Rarity.RARE);
	// Register a simple block and its BlockItem
	//@RenderLayer(Layer.CUTOUT)
//	@Category
//	public static final KiwiGO<TestBlock> FIRST_BLOCK = go(() -> new TestBlock2(blockProp()));

	// Register a simple effect
	public static final KiwiGO<MobEffect> FIRST_EFFECT = go(() -> new InstantenousMobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000));

	// And its potion
	public static final KiwiGO<Potion> FIRST_POTION = go(() -> new Potion(new MobEffectInstance(
			FIRST_EFFECT.holder().orElseThrow(),
			1800)));

//	public static final KiwiGO<BlockEntityType<TestBlockEntity>> FIRST_TILE = blockEntity(TestBlockEntity::new, null, FIRST_BLOCK);

//	public static final KiwiGO<TestBlock> TEX_BLOCK = go(() -> new TestBlock(blockProp()));
//	public static final KiwiGO<BlockEntityType<TexBlockEntity>> TEX_TILE = blockEntity(TexBlockEntity::new, null, TEX_BLOCK);

	public static TestModule INSTANCE;

	public static final KiwiGO<RecipeType<?>> RECIPE_TYPE = go(() -> {
		return new RecipeType<>() {
		};
	});

	//	@Override
	//	@Environment(EnvType.CLIENT)
	//	protected void clientInit(ClientInitEvent event) {
	//		ModBlockItem.INSTANT_UPDATE_TILES.add(FIRST_TILE);
	//		ModBlockItem.INSTANT_UPDATE_TILES.add(TEX_TILE);
	//		ItemBlockRenderTypes.setRenderLayer(TEX_BLOCK, EnumUtil.BLOCK_RENDER_TYPES::contains);
	//	}
	//
	//	@SubscribeEvent
	//	@Environment(EnvType.CLIENT)
	//	public void blockColors(ColorHandlerEvent.Block event) {
	//		BlockColors blockColors = event.getBlockColors();
	//		blockColors.register((state, level, pos, i) -> {
	//			BlockEntity blockEntity = level.getBlockEntity(pos);
	//			if (blockEntity instanceof RetextureBlockEntity) {
	//				return ((RetextureBlockEntity) blockEntity).getColor(level, i);
	//			}
	//			return -1;
	//		}, TEX_BLOCK);
	//	}
	//
	//	@Override
	//	protected void serverInit(ServerInitEvent event) {
	//		Scheduler.register(MyTask.ID, MyTask.class);
	//	}
	//
	//	@Override
	//	protected void gatherData(GatherDataEvent event) {
	//		DataGenerator gen = event.getGenerator();
	//		if (event.includeServer()) {
	//			gen.addProvider(new KiwiLootTableProvider(gen).add(TestBlockLoot::new, LootContextParamSets.BLOCK));
	//			gen.addProvider(new TestRecipeProvider(gen));
	//		}
	//	}
}

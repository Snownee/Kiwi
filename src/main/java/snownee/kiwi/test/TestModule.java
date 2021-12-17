package snownee.kiwi.test;

import net.minecraft.world.effect.HealthBoostMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;

@KiwiModule("test")
@KiwiModule.Optional(defaultEnabled = false)
@KiwiModule.Category("building_blocks")
//@KiwiModule.Subscriber(Bus.MOD)
public class TestModule extends AbstractModule {
	// Keep your fields `public static`

	// Register a simple item
	public static TestItem FIRST_ITEM = new TestItem(itemProp().rarity(Rarity.EPIC));

	// The next block will use this builder to build its BlockItem. After that this field will be null
	public static Item.Properties FIRST_BLOCK_ITEM_BUILDER = itemProp().rarity(Rarity.RARE);
	// Register a simple block and its BlockItem
	//@RenderLayer(Layer.CUTOUT)
	public static TestBlock FIRST_BLOCK = new TestBlock2(blockProp(Material.WOOD));

	// Register a simple effect
	public static MobEffect FIRST_EFFECT = new HealthBoostMobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000);

	// And its potion
	public static Potion FIRST_POTION = new Potion(new MobEffectInstance(FIRST_EFFECT, 1800));

	public static BlockEntityType<TestBlockEntity> FIRST_TILE = blockEntity(TestBlockEntity::new, null, FIRST_BLOCK);

	public static TestBlock TEX_BLOCK = new TestBlock(blockProp(Material.WOOD));
	public static BlockEntityType<TexBlockEntity> TEX_TILE = blockEntity(TexBlockEntity::new, null, TEX_BLOCK);

	public static TestModule INSTANCE;

	public static RecipeType<CraftingRecipe> RECIPE_TYPE = new RecipeType<>() {
	};

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

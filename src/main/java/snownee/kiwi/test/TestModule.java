package snownee.kiwi.test;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.effect.HealthBoostMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;
import snownee.kiwi.client.model.TextureModel;
import snownee.kiwi.data.provider.KiwiLootTableProvider;
import snownee.kiwi.item.ModBlockItem;
import snownee.kiwi.schedule.Scheduler;

@KiwiModule("test")
@KiwiModule.Optional(disabledByDefault = true)
@KiwiModule.Category("building_blocks")
@KiwiModule.Subscriber(Bus.MOD)
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

	public static BlockEntityType<?> FIRST_TILE = BlockEntityType.Builder.of(TestBlockEntity::new, FIRST_BLOCK).build(null);

	public static TexBlock TEX_BLOCK = new TexBlock(blockProp(Material.WOOD));
	public static BlockEntityType<?> TEX_TILE = BlockEntityType.Builder.of(TexBlockEntity::new, TEX_BLOCK).build(null);

	public static TestModule INSTANCE;

	//	public static RecipeType<CraftingRecipe> RECIPE_TYPE = new RecipeType<>() {};

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onModelBake(ModelBakeEvent event) {
		Block block = TestModule.FIRST_BLOCK;
		TextureModel.register(event, block, null, "top");
		TextureModel.registerInventory(event, block, "top");
		ModBlockItem.INSTANT_UPDATE_TILES.add(FIRST_TILE);
		block = TestModule.TEX_BLOCK;
		TextureModel.register(event, block, null, "wool");
		TextureModel.registerInventory(event, block, "wool");
		ModBlockItem.INSTANT_UPDATE_TILES.add(TEX_TILE);
	}

	@Override
	protected void serverInit(FMLServerStartingEvent event) {
		Scheduler.register(MyTask.ID, MyTask.class);
	}

	@Override
	protected void gatherData(GatherDataEvent event) {
		DataGenerator gen = event.getGenerator();
		if (event.includeServer()) {
			gen.addProvider(new KiwiLootTableProvider(gen).add(TestBlockLoot::new, LootContextParamSets.BLOCK));
			gen.addProvider(new TestRecipeProvider(gen));
		}
	}
}

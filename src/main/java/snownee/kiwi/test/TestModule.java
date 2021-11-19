package snownee.kiwi.test;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.data.DataGenerator;
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
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;
import snownee.kiwi.block.entity.RetextureBlockEntity;
import snownee.kiwi.data.provider.KiwiLootTableProvider;
import snownee.kiwi.item.ModBlockItem;
import snownee.kiwi.loader.event.ClientInitEvent;
import snownee.kiwi.loader.event.ServerInitEvent;
import snownee.kiwi.schedule.Scheduler;
import snownee.kiwi.util.EnumUtil;

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

	public static TestBlock TEX_BLOCK = new TestBlock(blockProp(Material.WOOD));
	public static BlockEntityType<?> TEX_TILE = BlockEntityType.Builder.of(TexBlockEntity::new, TEX_BLOCK).build(null);

	public static TestModule INSTANCE;

	//	public static RecipeType<CraftingRecipe> RECIPE_TYPE = new RecipeType<>() {};

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void clientInit(ClientInitEvent event) {
		ModBlockItem.INSTANT_UPDATE_TILES.add(FIRST_TILE);
		ModBlockItem.INSTANT_UPDATE_TILES.add(TEX_TILE);
		ItemBlockRenderTypes.setRenderLayer(TEX_BLOCK, EnumUtil.BLOCK_RENDER_TYPES::contains);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void blockColors(ColorHandlerEvent.Block event) {
		BlockColors blockColors = event.getBlockColors();
		blockColors.register((state, level, pos, i) -> {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (blockEntity instanceof RetextureBlockEntity) {
				return ((RetextureBlockEntity) blockEntity).getColor(level, i);
			}
			return -1;
		}, TEX_BLOCK);
	}

	@Override
	protected void serverInit(ServerInitEvent event) {
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

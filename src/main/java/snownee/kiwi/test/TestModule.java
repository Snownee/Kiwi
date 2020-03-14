package snownee.kiwi.test;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.HealthBoostEffect;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Subscriber.Bus;
import snownee.kiwi.client.model.TextureModel;
import snownee.kiwi.item.ModBlockItem;
import snownee.kiwi.schedule.Scheduler;

@KiwiModule(name = "test")
@KiwiModule.Optional(disabledByDefault = true)
@KiwiModule.Group("building_blocks")
@KiwiModule.Subscriber(Bus.MOD)
public class TestModule extends AbstractModule {
    // Keep your fields `public static final`

    // Register a simple item
    public static final TestItem FIRST_ITEM = new TestItem(itemProp().rarity(Rarity.EPIC));

    // The next block will use this builder to build its BlockItem. After that this field will be null
    public static final Item.Properties FIRST_BLOCK_ITEM_BUILDER = itemProp().rarity(Rarity.RARE);
    // Register a simple block and its BlockItem
    //@RenderLayer(Layer.CUTOUT)
    public static final TestBlock FIRST_BLOCK = init(new TestBlock2(blockProp(Material.WOOD)));

    // Register a simple effect
    public static final Effect FIRST_EFFECT = new HealthBoostEffect(EffectType.BENEFICIAL, 0xFF0000);

    // And its potion
    public static final Potion FIRST_POTION = new Potion(new EffectInstance(FIRST_EFFECT, 1800));

    public static final TileEntityType<?> FIRST_TILE = TileEntityType.Builder.create(TestTile::new, FIRST_BLOCK).build(null);

    public static TestModule INSTANCE;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event) {
        Block block = TestModule.FIRST_BLOCK;
        TextureModel.register(event, block, null, "top");
        TextureModel.registerInventory(event, block, "top");
        ModBlockItem.INSTANT_UPDATE_TILES.add(FIRST_TILE);
    }

    @Override
    protected void serverInit(FMLServerStartingEvent event) {
        Scheduler.register(MyTask.ID, MyTask.class);
    }
}

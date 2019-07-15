package snownee.kiwi.test;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.HealthBoostEffect;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.client.model.TextureModel;

@KiwiModule(modid = Kiwi.MODID, name = "test")
@KiwiModule.Optional(disabledByDefault = true)
@KiwiModule.Group("building_blocks")
public class TestModule extends AbstractModule
{
    public TestModule()
    {
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    // Keep your fields `public static final`

    // Register a simple item
    public static final TestItem FIRST_ITEM = new TestItem(itemProp().rarity(Rarity.EPIC));

    // The next block will use this builder to build its BlockItem. After that this field will be null
    public static final Item.Properties FIRST_BLOCK_ITEM_BUILDER = itemProp().rarity(Rarity.RARE);
    // Register a simple block and its BlockItem
    public static final TestBlock FIRST_BLOCK = new TestBlock(blockProp(Material.WOOD));

    // Register a simple effect
    public static final Effect FIRST_EFFECT = new HealthBoostEffect(EffectType.BENEFICIAL, 0xFF0000);

    // And its potion
    public static final Potion FIRST_POTION = new Potion(new EffectInstance(FIRST_EFFECT, 1800));

    public static final TileEntityType<?> FIRST_TILE = TileEntityType.Builder.create(TestTile::new, FIRST_BLOCK).build(null);

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event)
    {
        Block block = TestModule.FIRST_BLOCK;
        BlockState state = block.getDefaultState().with(StairsBlock.FACING, Direction.EAST);
        TextureModel.register(event, block, state);
    }
}

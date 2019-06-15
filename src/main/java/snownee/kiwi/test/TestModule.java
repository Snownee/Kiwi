package snownee.kiwi.test;

import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.HealthBoostEffect;
import net.minecraft.potion.Potion;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.block.ModBlock;

@KiwiModule(modid = Kiwi.MODID, name = "test")
@KiwiModule.Optional(disabledByDefault = true)
@KiwiModule.Group("buildingBlocks")
public class TestModule extends AbstractModule
{
    // Register a simple item
    public static final TestItem FIRST_ITEM = new TestItem(itemProp().rarity(Rarity.EPIC));

    public static final Item.Properties FIRST_BLOCK_ITEM_BUILDER = itemProp().rarity(Rarity.RARE);
    // Register a simple block and its BlockItem
    public static final ModBlock FIRST_BLOCK = new ModBlock(blockProp(Material.WOOD));

    // Register a simple effect
    public static final Effect FIRST_EFFECT = new HealthBoostEffect(EffectType.BENEFICIAL, 0xFF0000);

    // And its potion
    public static final Potion FIRST_POTION = new Potion(new EffectInstance(FIRST_EFFECT, 1800));
}

package snownee.kiwi.util.definition;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IRegistryDelegate;
import snownee.kiwi.Kiwi;
import snownee.kiwi.crafting.input.ProcessingInput;

/**
 * Comparable, NBT-insensitive, size-insensitive item definition that may be used as key of Map.
 *
 * 可比较的物品定义信息，忽略 NBT 数据及数量，可用作 Map 的键。
 */
// In 1.13 this will be obsoleted due to the removal of item metadata
public final class ItemDefinition implements Comparable<ItemDefinition>, ProcessingInput
{
    public static final ItemDefinition EMPTY = of(Items.AIR);

    public static ItemDefinition of(Item item)
    {
        return of(item, 0);
    }

    public static ItemDefinition of(ItemStack stack)
    {
        return stack.isEmpty() ? of(Items.AIR) : of(stack.getItem(), stack.getHasSubtypes() ? stack.getMetadata() : 0);
    }

    public static ItemDefinition of(Block block)
    {
        return of(Item.getItemFromBlock(block));
    }

    public static ItemDefinition of(Block block, int metadata)
    {
        return of(Item.getItemFromBlock(block), metadata);
    }

    public static ItemDefinition of(Item item, int metadata)
    {
        return new ItemDefinition(item, metadata);
    }

    //private static final Pattern ITEM_DEFINITION_FORMAT = Pattern.compile("(?<namespace>[\\w\\-]+):(?<path>[^A-Z]+):(?<metadata>[0-9]+)");

    public static ItemDefinition parse(String string, boolean allowWildcard)
    {
        String[] parts = string.split(":");
        if (parts.length >= 2 && parts.length <= 3)
        {
            int meta = 0;
            if (parts.length == 3)
            {
                if (allowWildcard && parts[2].equals("*"))
                {
                    meta = OreDictionary.WILDCARD_VALUE;
                }
                else
                {
                    try
                    {
                        meta = Integer.parseInt(parts[2]);
                    }
                    catch (Exception e)
                    {
                    }
                    if (!allowWildcard && meta == OreDictionary.WILDCARD_VALUE)
                    {
                        meta = 0;
                    }
                }
            }
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(parts[0], parts[1]));
            if (item != null && item != Items.AIR)
            {
                return new ItemDefinition(item, meta);
            }
        }
        Kiwi.logger.error("Fail to parse \"{}\" to ItemDefinition.", string);
        return ItemDefinition.EMPTY;
    }

    private final IRegistryDelegate<Item> item;
    private final int metadata;

    public ItemDefinition(Item item, int metadata)
    {
        this.item = item.delegate;
        this.metadata = metadata;
    }

    public Item getItem()
    {
        return item.get();
    }

    public int getMetadata()
    {
        return metadata;
    }

    @Nonnull
    @Override
    public List<ItemStack> examples()
    {
        if (metadata == OreDictionary.WILDCARD_VALUE)
        {
            CreativeTabs itemGroup = item.get().getCreativeTab();
            if (itemGroup != null)
            {
                NonNullList<ItemStack> stacks = NonNullList.create();
                item.get().getSubItems(itemGroup, stacks);
                return stacks;
            }
            else
            {
                return Collections.emptyList();
            }
        }
        else
        {
            return Collections.singletonList(this.getItemStack());
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        ItemDefinition that = (ItemDefinition) o;
        return this.item.equals(that.item) && this.metadata == that.metadata;
    }

    @Override
    public boolean matches(ItemStack stack)
    {
        return stack.getItem().delegate == this.item && (OreDictionary.WILDCARD_VALUE == metadata || stack.getMetadata() == metadata);
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec
     * This provides a size-insensitive implementation.
     *
     * @return Constant of one (1).
     */
    @Override
    public int count()
    {
        return 1;
    }

    @Override
    public int hashCode()
    {
        return item.hashCode() * 31 + metadata;
    }

    @Override
    public int compareTo(ItemDefinition o)
    {
        int result = this.item.name().compareTo(o.item.name());
        return result == 0 ? this.metadata - o.metadata : result;
    }

    @Override
    public String toString()
    {
        return item.name() + ":" + (metadata != OreDictionary.WILDCARD_VALUE ? metadata : "*");
    }

    /**
     * 将该 ItemDefinition 对象转换为 ItemStack 对象.
     * @return The only possible permutation of this ItemDefinition, in ItemStack form
     */
    public ItemStack getItemStack()
    {
        // TODO (3TUSK): YOU CANNOT USE OreDictionary.WILDCARD HERE! Unless we can come up a restriction on how to use this return value.
        return new ItemStack(this.item.get(), 1, this.metadata, null);
    }

    @Override
    public boolean isEmpty()
    {
        return item.get() == Items.AIR;
    }
}

package snownee.kiwi.util.definition;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
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

    public static ItemDefinition parse(String string, boolean allowWildcard)
    {
        String[] parts = string.split(":");
        if (parts.length >= 2 && parts.length <= 3)
        {
            int meta = (allowWildcard && parts.length == 3) ? -1 : 0;
            if (parts.length == 3)
            {
                if (allowWildcard && parts[2].equals("*"))
                {
                    meta = OreDictionary.WILDCARD_VALUE;
                }
                else
                {
                    meta = Integer.parseInt(parts[2]);
                }
            }
            if (meta >= 0)
            {
                Item item = Item.getByNameOrId(parts[0] + ":" + parts[1]);
                if (item != null && item != Items.AIR)
                {
                    return new ItemDefinition(item, meta);
                }
            }
        }
        Kiwi.logger.error("Fail to parse \"{}\" to ItemDefinition.", string);
        return ItemDefinition.of(Items.AIR);
    }

    private final Item item;
    private final int metadata;

    public ItemDefinition(Item item, int metadata)
    {
        this.item = item;
        this.metadata = metadata;
    }

    public Item getItem()
    {
        return item;
    }

    public int getMetadata()
    {
        return metadata;
    }

    @Override
    public NonNullList<ItemStack> examples()
    {
        NonNullList<ItemStack> stacks = NonNullList.create();
        if (metadata == OreDictionary.WILDCARD_VALUE && item.getCreativeTab() != null)
        {
            item.getSubItems(item.getCreativeTab(), stacks);
        }
        else
        {
            stacks.add(this.getItemStack());
        }
        return stacks;
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
        return this.item == that.item && this.metadata == that.metadata;
    }

    @Override
    public boolean matches(ItemStack stack)
    {
        return stack.getItem() == this.item && (OreDictionary.WILDCARD_VALUE == metadata || stack.getMetadata() == metadata);
    }

    /**
     * Size-insensitive implementation.
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
        int result = this.item.getRegistryName().compareTo(o.item.getRegistryName());
        return result == 0 ? this.metadata - o.metadata : result;
    }

    @Override
    public String toString()
    {
        return item.getRegistryName() + ":" + (metadata != OreDictionary.WILDCARD_VALUE ? metadata : "*");
    }

    /**
     * 将该 ItemDefinition 对象转换为 ItemStack 对象.
     * @return The only possible permutation of this ItemDefinition, in ItemStack form
     */
    public ItemStack getItemStack()
    {
        return new ItemStack(this.item, 1, this.metadata, null);
    }

    @Override
    public boolean isEmpty()
    {
        return item == Items.AIR;
    }
}

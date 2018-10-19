package snownee.kiwi.crafting.input;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * NBT-insensitive recipe input implementation. It also serves as a
 * demonstrative implementation of {@link ProcessingInput}.
 */
public class RegularItemStackInput implements ProcessingInput
{

    private final Item item;
    private final int meta, size;

    public static RegularItemStackInput of(@Nonnull ItemStack instance)
    {
        return instance.isEmpty() ? of(Items.AIR) : new RegularItemStackInput(instance.getItem(), instance.getMetadata(), instance.getCount());
    }

    public static RegularItemStackInput of(@Nonnull Block block)
    {
        Item actualForm = Item.getItemFromBlock(block);
        if (actualForm == Items.AIR)
        {
            throw new IllegalArgumentException("Block %s does not have item form");
        }
        return of(actualForm);
    }

    public static RegularItemStackInput of(@Nonnull Block block, int meta)
    {
        Item actualForm = Item.getItemFromBlock(block);
        if (actualForm == Items.AIR)
        {
            throw new IllegalArgumentException("Block %s does not have item form");
        }
        return of(actualForm, meta);
    }

    public static RegularItemStackInput of(@Nonnull Item item)
    {
        return new RegularItemStackInput(item, 0, 1);
    }

    public static RegularItemStackInput of(@Nonnull Item item, int meta)
    {
        return new RegularItemStackInput(item, meta, 1);
    }

    protected RegularItemStackInput(@Nonnull Item item, int meta, int size)
    {
        this.item = item;
        this.meta = meta;
        this.size = size;
    }

    @Override
    public List<ItemStack> examples()
    {
        return isEmpty() ? Collections.emptyList() : Collections.singletonList(new ItemStack(item, size, meta));
    }

    @Override
    public boolean matches(ItemStack stack)
    {
        return stack.isEmpty() ? this.isEmpty() : (stack.getItem() == this.item && stack.getMetadata() == this.meta && stack.getCount() >= this.size);
    }

    @Override
    public int count()
    {
        return this.isEmpty() ? 0 : this.size;
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

        RegularItemStackInput that = (RegularItemStackInput) o;

        if (meta != that.meta)
        {
            return false;
        }
        if (size != that.size)
        {
            return false;
        }
        return item == that.item;
    }

    @Override
    public int hashCode()
    {
        int result = item.hashCode();
        result = 31 * result + meta;
        result = 31 * result + size;
        return result;
    }

    @Override
    public boolean isEmpty()
    {
        return item == Items.AIR;
    }
}

//package snownee.kiwi.item;
//
//import java.util.Arrays;
//import java.util.List;
//import net.minecraft.creativetab.CreativeTabs;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.NonNullList;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//import snownee.kiwi.client.ModelUtil;
//
//public class ItemModVariantsNew<T, E extends IVariant<T>> extends ModItem
//{
//    private final E[] values;
//
//    public ItemModVariantsNew(String name, E[] values)
//    {
//        super(name);
//        this.values = values;
//        setHasSubtypes(values.length > 1);
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void mapModel()
//    {
//        ModelUtil.mapItemVariantsModelNew(this, getName() + "_", this.values, "");
//    }
//
//    public List<E> getVariants()
//    {
//        return Arrays.asList(values);
//    }
//
//    public ItemStack getItemStack(E variant)
//    {
//        return getItemStack(variant, 1);
//    }
//
//    public ItemStack getItemStack(E variant, int amount)
//    {
//        return new ItemStack(this, amount, variant.getMeta());
//    }
//
//    @Override
//    public String getTranslationKey(ItemStack stack)
//    {
//        if (stack.getMetadata() < values.length)
//        {
//            return super.getTranslationKey(stack) + "." + values[stack.getMetadata()].getName();
//        }
//        else
//        {
//            return super.getTranslationKey(stack);
//        }
//    }
//
//    @Override
//    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
//    {
//        if (this.isInCreativeTab(tab))
//        {
//            getVariants().forEach(v -> items.add(getItemStack(v)));
//        }
//    }
//
//}

//package snownee.kiwi.item;
//
//import net.minecraft.creativetab.CreativeTabs;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.IStringSerializable;
//import net.minecraft.util.NonNullList;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//
//import org.apache.commons.lang3.Validate;
//import snownee.kiwi.client.ModelUtil;
//import snownee.kiwi.util.VariantsHolder;
//import snownee.kiwi.util.VariantsHolder.Variant;
//
//public class ItemModVariants extends ModItem
//{
//    private final VariantsHolder<IStringSerializable> variants;
//
//    public ItemModVariants(String name, VariantsHolder<IStringSerializable> variants)
//    {
//        super(name);
//        this.variants = variants;
//        setHasSubtypes(this.variants.size() > 1);
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void mapModel()
//    {
//        ModelUtil.mapItemVariantsModel(this, getName() + "_", variants, "");
//    }
//
//    public VariantsHolder getVariants()
//    {
//        return variants;
//    }
//
//    public ItemStack getItemStack(Variant variant)
//    {
//        return getItemStack(variant, 1);
//    }
//
//    public ItemStack getItemStack(Variant variant, int amount)
//    {
//        int meta = variants.indexOf(variant);
//        Validate.inclusiveBetween(0, variants.size(), meta);
//        return new ItemStack(this, amount, meta);
//    }
//
//    @Override
//    public String getTranslationKey(ItemStack stack)
//    {
//        if (stack.getMetadata() < variants.size())
//        {
//            return super.getTranslationKey(stack) + "." + variants.get(stack.getMetadata()).getValue().getName();
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
//            variants.forEach(v -> items.add(getItemStack(v)));
//        }
//    }
//}

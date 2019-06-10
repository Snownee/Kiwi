//package snownee.kiwi.util.definition;
//
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//import net.minecraft.item.ItemStack;
//import snownee.kiwi.crafting.input.ProcessingInput;
//import snownee.kiwi.util.OreUtil;
//
//public class OreDictDefinition implements ProcessingInput
//{
//    public static final OreDictDefinition EMPTY = of("", 0);
//
//    String ore;
//    int size;
//
//    private OreDictDefinition(String ore)
//    {
//        this(ore, 1);
//    }
//
//    private OreDictDefinition(String ore, int size)
//    {
//        this.ore = ore;
//        this.size = size;
//    }
//
//    public static OreDictDefinition of(String ore)
//    {
//        return new OreDictDefinition(ore);
//    }
//
//    public static OreDictDefinition of(String ore, int size)
//    {
//        return new OreDictDefinition(ore, size);
//    }
//
//    @Override
//    public List<ItemStack> examples()
//    {
//        return isEmpty() ? Collections.emptyList() : OreUtil.getItemsFromOre(ore, size);
//    }
//
//    public ItemStack getItemStack()
//    {
//        return OreUtil.getPreferredItemFromOre(ore);
//    }
//
//    @Override
//    public boolean matches(ItemStack stack)
//    {
//        return stack.getCount() >= size && OreUtil.doesItemHaveOreName(stack, ore);
//    }
//
//    @Override
//    public boolean equals(Object o)
//    {
//        if (this == o)
//        {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass())
//        {
//            return false;
//        }
//        OreDictDefinition that = (OreDictDefinition) o;
//        return size == that.size && ore.equals(that.ore);
//    }
//
//    @Override
//    public int count()
//    {
//        return this.size;
//    }
//
//    @Override
//    public String toString()
//    {
//        return "ore:" + (isEmpty() ? "null" : ore) + " * " + size;
//    }
//
//    @Override
//    public int hashCode()
//    {
//        return ore.hashCode() * 31 + size;
//    }
//
//    @Override
//    public boolean isEmpty()
//    {
//        return ore.isEmpty() || size == 0;
//    }
//}
//
//final class WeirdCompartor implements Comparator<ProcessingInput>
//{
//
//    @Override
//    public int compare(ProcessingInput a, ProcessingInput b)
//    {
//        if (a instanceof OreDictDefinition)
//        {
//            if (b instanceof OreDictDefinition)
//            {
//                int result = ((OreDictDefinition) a).ore.compareTo(((OreDictDefinition) b).ore);
//                return result == 0 ? Integer.compare(((OreDictDefinition) b).size, ((OreDictDefinition) a).size) : result;
//            }
//            else
//            {
//                return -1;
//            }
//        }
//        else
//        {
//            return b instanceof OreDictDefinition ? 1 : 0;
//        }
//    }
//}

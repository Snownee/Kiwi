package snownee.kiwi.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.util.IStringSerializable;
import snownee.kiwi.util.VariantsHolder.Variant;

public class VariantsHolder<T extends IStringSerializable> extends AbstractList<Variant<T>>
{
    // ImmutableList? a builder?
    private final List<Variant<T>> holder = new ArrayList<>();

    public Variant<T> addVariant(T value)
    {
        Variant<T> variant = new Variant<>(value, holder.size());
        holder.add(variant);
        return variant;
    }

    public VariantsHolder<T> addVariants(T[] values)
    {
        for (T value : values)
        {
            holder.add(new Variant<>(value, holder.size()));
        }
        return this;
    }

    public static class Type implements IStringSerializable
    {
        private final String name;

        public Type(String name)
        {
            this.name = name;
        }

        @Override
        public String getName()
        {
            return name;
        }
    }

    public static class Variant<T extends IStringSerializable>
    {
        private final T value;
        private final int meta;

        private Variant(T type, int meta)
        {
            this.value = type;
            this.meta = meta;
        }

        public T getValue()
        {
            return value;
        }

        public int getMeta()
        {
            return meta;
        }
    }

    @Override
    public int size()
    {
        return holder.size();
    }

    @Override
    public boolean isEmpty()
    {
        return holder.isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        return holder.contains(o);
    }

    @Override
    public Iterator<Variant<T>> iterator()
    {
        return holder.iterator();
    }

    @Override
    public Object[] toArray()
    {
        return holder.toArray();
    }

    @Override
    public boolean add(Variant<T> e)
    {
        return holder.add(e);
    }

    @Override
    public boolean remove(Object o)
    {
        return holder.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return holder.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Variant<T>> c)
    {
        return holder.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Variant<T>> c)
    {
        return holder.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return holder.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return holder.retainAll(c);
    }

    @Override
    public void clear()
    {
        holder.clear();
    }

    @Override
    public Variant<T> get(int index)
    {
        return holder.get(index);
    }

    @Override
    public Variant<T> set(int index, Variant<T> element)
    {
        return holder.set(index, element);
    }

    @Override
    public void add(int index, Variant<T> element)
    {
        holder.add(index, element);
    }

    @Override
    public Variant<T> remove(int index)
    {
        return holder.remove(index);
    }

    @Override
    public int indexOf(Object o)
    {
        return holder.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return holder.lastIndexOf(o);
    }

    @Override
    public ListIterator<Variant<T>> listIterator()
    {
        return holder.listIterator();
    }

    @Override
    public ListIterator<Variant<T>> listIterator(int index)
    {
        return holder.listIterator(index);
    }

    @Override
    public List<Variant<T>> subList(int fromIndex, int toIndex)
    {
        return holder.subList(fromIndex, toIndex);
    }

}

package snownee.kiwi.item;

import java.util.Locale;

public interface IVariant<T>
{
    default String getName()
    {
        return toString().toLowerCase(Locale.ENGLISH);
    }

    int getMeta();

    T getValue();
}

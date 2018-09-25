package snownee.kiwi;

public interface IModule
{
    default void preInit()
    {
        // NO-OP
    }

    default void init()
    {
        // NO-OP
    }

    default void postInit()
    {
        // NO-OP
    }
}

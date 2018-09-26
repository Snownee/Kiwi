# Kiwi

Make modding no longer cumbersome.

## Registration

``` java
@KiwiModule(modid = MyMod.MODID)
public class ModRegistry implements IModule
{
    // Register a simple item. Kiwi will automatically register and map models
    public static final ItemMod FIRST_Item = new ItemMod("my_first_item");

    // Register a simple block and its ItemBlock
    public static final BlockMod FIRST_BLOCK = new BlockMod("my_first_block");
}
```

## Plugin

``` java
// This module will be loaded will when `dependency` mod is loaded
@KiwiModule(modid = MyAddon.MODID, name = "dependency", dependency = "dependency")
public class ModAddon implements IModule
{
    // This method is called in FMLInitializationEvent
    @Override
    public void init()
    {
        // Do something
        OreDictionary.registerOre("exampleOre", exampleItem);
    }
}
```

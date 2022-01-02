# Kiwi

Kiwi is a Minecraft modding library designed to help developers focus on content creation instead of repetitive work.

[中文指南](https://moddingwithkiwi.readthedocs.io/zh_CN/1.18/)

## Registration

``` java
@KiwiModule
@KiwiModule.Category("building_blocks")
public class MyModule extends AbstractModule
{
    // Register a simple item. Kiwi will automatically register it
    public static Item FIRST_ITEM = new Item(itemProp().rarity(Rarity.EPIC));

    // Register a simple block and its BlockItem
    public static Block FIRST_BLOCK = new Block(blockProp(Material.WOOD));
}
```

## Plugin

``` java
// This module will be loaded only when `dependency` mod is loaded
@KiwiModule(value = "test", dependency = "modid")
@KiwiModule.Optional
public class MyAddon extends AbstractModule
{
    // This method is called in FMLCommonSetupEvent
    @Override
    public void init(InitEvent event)
    {
        // Do something
    }
}
```

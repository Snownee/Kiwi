# Kiwi

Kiwi is a Minecraft modding library designed to help developers focus on content creation instead of repetitive work.

[中文指南（已过期）](https://moddingwithkiwi.readthedocs.io/zh_CN/1.18.2/)

## Registration

``` java
@KiwiModule
@KiwiModule.Category(Categories.BUILDING_BLOCKS)
public class MyModule extends AbstractModule
{
    // Register a simple item. Kiwi will automatically register it
    public static final KiwiGO<Item> FIRST_ITEM = go(() -> new Item(itemProp().rarity(Rarity.EPIC)));

    // Register a simple block and its BlockItem
    public static final KiwiGO<Block> FIRST_BLOCK = go(() -> new Block(blockProp()));
}
```

## Conditional Loading

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

## Configuration

```java
@KiwiConfig(type = ConfigType.COMMON)
public class MyConfig {
    // Full Cloth Config integration. Translations are generated from data-gen
    @KiwiConfig.Path("debugMode.enabled")
    public static boolean debug = true;

    @Range(min = 0, max = 1)
    public static float treasureChance = 0.5f;

    @KiwiConfig.Listen("debugMode.enabled")
    public static void onToggleDebugMode(String path) {
        System.out.println("Debug Mode: " + debug);
    }
}
```



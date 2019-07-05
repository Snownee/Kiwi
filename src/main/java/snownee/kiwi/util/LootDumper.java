package snownee.kiwi.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class LootDumper
{
    private LootDumper()
    {
    }

    public static void dump(String regex)
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null)
        {
            classLoader = Class.class.getClassLoader();
        }
        InputStream in = classLoader.getResourceAsStream("snownee/kiwi/util/sample.json");
        byte[] bytes;
        try
        {
            bytes = new byte[in.available()];
            in.read(bytes);
        }
        catch (IOException | NullPointerException e)
        {
            e.printStackTrace();
            return;
        }
        String sample = new String(bytes);

        for (ResourceLocation rl : ForgeRegistries.BLOCKS.getKeys())
        {
            if (ForgeRegistries.ITEMS.containsKey(rl) && rl.toString().matches(regex))
            {
                File dir = new File(Minecraft.getInstance().gameDir, "dumps/data/" + rl.getNamespace() + "/loot_tables/blocks");
                File file = new File(dir, rl.getPath() + ".json");
                try
                {
                    dir.mkdirs();
                    file.createNewFile();
                    FileWriter writer = new FileWriter(file);
                    writer.write(String.format(sample, rl));
                    writer.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}

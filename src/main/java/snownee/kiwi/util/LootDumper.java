package snownee.kiwi.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class LootDumper
{
    private LootDumper()
    {
    }

    public static int dump(Predicate<String> matcher, File dataDir)
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
            return 0;
        }
        String sample = new String(bytes);

        int count = 0;
        for (ResourceLocation rl : ForgeRegistries.BLOCKS.getKeys())
        {
            if (ForgeRegistries.ITEMS.containsKey(rl) && matcher.test(rl.toString()))
            {
                File dir = new File(dataDir, "dumps/data/" + rl.getNamespace() + "/loot_tables/blocks");
                File file = new File(dir, rl.getPath() + ".json");
                try
                {
                    dir.mkdirs();
                    file.createNewFile();
                    FileWriter writer = new FileWriter(file);
                    writer.write(String.format(sample, rl));
                    writer.close();
                    ++count;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return count;
                }
            }
        }
        return count;
    }
}

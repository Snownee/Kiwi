package snownee.kiwi.tile;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.client.model.TextureModel;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.NBTHelper.NBT;
import snownee.kiwi.util.Util;

public class TextureTile extends BaseTile
{
    protected Map<String, String> textures;
    protected Map<String, Item> marks;
    protected IModelData modelData;

    public TextureTile(TileEntityType<?> tileEntityTypeIn, String... textureKeys)
    {
        super(tileEntityTypeIn);
        persistData = true;
        textures = Maps.newHashMapWithExpectedSize(textureKeys.length);
        for (String key : textureKeys)
        {
            textures.put(key, "");
        }
        if (EffectiveSide.get() == LogicalSide.CLIENT)
        {
            modelData = new ModelDataMap.Builder().withInitial(TextureModel.TEXTURES, textures).build();
        }
    }

    public void setTexture(String key, String path)
    {
        setTexture(textures, key, path);
    }

    public static void setTexture(Map<String, String> textures, String key, String path)
    {
        if (!textures.containsKey(key))
        {
            return;
        }
        textures.put(key, path);
    }

    public void setTexture(String key, BlockState state)
    {
        setTexture(textures, key, state);
        if (isMark(key))
        {
            Item item = state.getBlock().asItem();
            if (item == null)
            {
                return;
            }
            if (marks == null)
            {
                marks = Maps.newHashMap();
            }
            marks.put(key, item);
        }
    }

    public static void setTexture(Map<String, String> textures, String key, BlockState state)
    {
        if (!textures.containsKey(key))
        {
            return;
        }
        if (EffectiveSide.get() == LogicalSide.SERVER)
        {
            String value = NBTUtil.writeBlockState(state).toString();
            textures.put(key, value);
        }
        else
        {
            textures.put(key, getTextureFromState(state));
        }
    }

    public void setTexture(String key, Item item)
    {
        setTexture(textures, key, item);
        if (isMark(key))
        {
            if (marks == null)
            {
                marks = Maps.newHashMap();
            }
            marks.put(key, item);
        }
    }

    public static void setTexture(Map<String, String> textures, String key, Item item)
    {
        Block block = Block.getBlockFromItem(item);
        if (block != null)
        {
            setTexture(textures, key, block.getDefaultState());
        }
    }

    /**
     * @since 2.3.0
     */
    public Item getMark(String key)
    {
        return marks.getOrDefault(key, Items.AIR);
    }

    @OnlyIn(Dist.CLIENT)
    public static String getTextureFromState(BlockState state)
    {
        return Util.trimRL(Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state).getName());
    }

    @Override
    public void refresh()
    {
        if (world != null && world.isRemote)
        {
            requestModelDataUpdate();
        }
        else
        {
            super.refresh();
        }
    }

    @Override
    public void onLoad()
    {
        super.requestModelDataUpdate();
    }

    @Override
    public void requestModelDataUpdate()
    {
        super.requestModelDataUpdate();
        if (world != null && world.isRemote)
        {
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 8);
        }
    }

    @Override
    protected void readPacketData(CompoundNBT data)
    {
        if (!data.contains("Textures", NBT.COMPOUND))
        {
            return;
        }
        boolean shouldRefresh = readTextures(textures, data.getCompound("Textures"));
        if (data.contains("Items", NBT.COMPOUND))
        {
            NBTHelper helper = NBTHelper.of(data.getCompound("Items"));
            for (String k : helper.keySet(""))
            {
                if (!isMark(k))
                {
                    continue;
                }
                if (marks == null)
                {
                    marks = Maps.newHashMap();
                }
                ResourceLocation locator = ResourceLocation.tryCreate(helper.getString(k));
                if (locator != null)
                {
                    Item item = ForgeRegistries.ITEMS.getValue(locator);
                    if (item != null)
                    {
                        marks.put(k, item);
                    }
                }
            }
        }
        if (shouldRefresh)
        {
            refresh();
        }
    }

    public static boolean readTextures(Map<String, String> textures, CompoundNBT data)
    {
        boolean shouldRefresh = false;
        NBTHelper helper = NBTHelper.of(data);
        for (String k : textures.keySet())
        {
            String v = helper.getString(k);
            if (v == null)
                continue;
            if (EffectiveSide.get() == LogicalSide.CLIENT && v.startsWith("{"))
            {
                try
                {
                    CompoundNBT stateNbt = JsonToNBT.getTagFromJson(v);
                    BlockState state = NBTUtil.readBlockState(stateNbt);
                    v = getTextureFromState(state);
                }
                catch (CommandSyntaxException e)
                {
                    e.printStackTrace();
                    continue;
                }
            }
            if (!textures.get(k).equals(v))
            {
                shouldRefresh = true;
                textures.put(k, v);
            }
        }
        return shouldRefresh;
    }

    public boolean isMark(String key)
    {
        return false;
    }

    @Override
    protected CompoundNBT writePacketData(CompoundNBT data)
    {
        writeTextures(textures, data);
        if (marks != null)
        {
            NBTHelper helper = NBTHelper.of(data);
            marks.forEach((k, v) -> {
                if (isMark(k))
                    helper.setString("Items." + k, Util.trimRL(v.getRegistryName()));
            });
        }
        return data;
    }

    public static CompoundNBT writeTextures(Map<String, String> textures, CompoundNBT data)
    {
        NBTHelper tag = NBTHelper.of(data);
        textures.forEach((k, v) -> tag.setString("Textures." + k, v));
        return data;
    }

    @Override
    public IModelData getModelData()
    {
        return modelData;
    }
}

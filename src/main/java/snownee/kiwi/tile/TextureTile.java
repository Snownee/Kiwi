package snownee.kiwi.tile;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import snownee.kiwi.client.model.TextureModel;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.NBTHelper.NBT;

public class TextureTile extends BaseTile
{
    protected Map<String, String> textures;
    protected IModelData modelData;

    public TextureTile(TileEntityType<?> tileEntityTypeIn, String... textureKeys)
    {
        super(tileEntityTypeIn);
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
        if (!textures.containsKey(key))
        {
            return;
        }
        textures.put(key, path);
    }

    public void setTexture(String key, BlockState state)
    {
        if (!textures.containsKey(key))
        {
            return;
        }
        if (!world.isRemote)
        {
            String value = NBTUtil.writeBlockState(state).toString();
            textures.put(key, value);
        }
        else
        {
            textures.put(key, getTextureFromState(state));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static String getTextureFromState(BlockState state)
    {
        return Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state).getName().toString();
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
        readTextures(textures, data.getCompound("Textures"));
        refresh();
    }

    public static void readTextures(Map<String, String> textures, CompoundNBT data)
    {
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
                    textures.put(k, getTextureFromState(state));
                }
                catch (CommandSyntaxException e)
                {
                    e.printStackTrace();
                    continue;
                }
            }
            else
            {
                textures.put(k, v);
            }
        }
    }

    @Override
    protected CompoundNBT writePacketData(CompoundNBT data)
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

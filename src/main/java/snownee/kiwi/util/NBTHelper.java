package snownee.kiwi.util;

import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class NBTHelper
{
    public static class Tag
    {
        public static final int END = NBT.TAG_END;
        public static final int BYTE = NBT.TAG_BYTE;
        public static final int SHORT = NBT.TAG_SHORT;
        public static final int INT = NBT.TAG_INT;
        public static final int LONG = NBT.TAG_LONG;
        public static final int FLOAT = NBT.TAG_FLOAT;
        public static final int DOUBLE = NBT.TAG_DOUBLE;
        public static final int BYTE_ARRAY = NBT.TAG_BYTE_ARRAY;
        public static final int STRING = NBT.TAG_STRING;
        public static final int LIST = NBT.TAG_LIST;
        public static final int COMPOUND = NBT.TAG_COMPOUND;
        public static final int INT_ARRAY = NBT.TAG_INT_ARRAY;
        public static final int LONG_ARRAY = NBT.TAG_LONG_ARRAY;
        public static final int ANY_NUMERIC = NBT.TAG_ANY_NUMERIC;
    }

    @Nullable
    private ItemStack stack;
    @Nullable
    private NBTTagCompound tag;

    private NBTHelper(@Nullable NBTTagCompound tag, @Nullable ItemStack stack)
    {
        this.stack = stack;
        this.tag = tag;
    }

    public NBTTagCompound getTag(String key)
    {
        return getTag(key, false);
    }

    public NBTTagCompound getTag(String key, boolean createIfNull)
    {
        return getTagInternal(key, createIfNull, false);
    }

    private NBTTagCompound getTagInternal(String key, boolean createIfNull, boolean ignoreLastNode)
    {
        if (tag == null)
        {
            if (createIfNull)
            {
                tag = new NBTTagCompound();
                if (stack != null)
                {
                    stack.setTagCompound(tag);
                }
            }
            else
            {
                return null;
            }
        }
        if (key.isEmpty())
        {
            return tag;
        }
        NBTTagCompound subTag = tag;
        String[] parts = key.split("\\.");
        int length = parts.length;
        if (ignoreLastNode)
        {
            --length;
        }
        for (int i = 0; i < length; ++i)
        {
            // TODO: list support. e.g. a.b[2].c.d
            if (!subTag.hasKey(parts[i], Tag.COMPOUND))
            {
                if (createIfNull)
                {
                    subTag.setTag(parts[i], new NBTTagCompound());
                }
                else
                {
                    return null;
                }
            }
            subTag = subTag.getCompoundTag(parts[i]);
        }
        return subTag;
    }

    private NBTTagCompound getTagInternal(String key)
    {
        return getTagInternal(key, true, true);
    }

    private String getLastNode(String key)
    {
        int index = key.lastIndexOf(".");
        if (index < 0)
        {
            return key;
        }
        else
        {
            return key.substring(index + 1);
        }
    }

    public NBTHelper setTag(String key, NBTBase value)
    {
        getTagInternal(key).setTag(getLastNode(key), value);
        return this;
    }

    public NBTHelper setInt(String key, int value)
    {
        getTagInternal(key).setInteger(getLastNode(key), value);
        return this;
    }

    public int getInt(String key)
    {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.hasKey(actualKey, Tag.INT))
            {
                return subTag.getInteger(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setLong(String key, long value)
    {
        getTagInternal(key).setLong(getLastNode(key), value);
        return this;
    }

    public long getLong(String key)
    {
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultValue)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.hasKey(actualKey, Tag.LONG))
            {
                return subTag.getLong(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setShort(String key, short value)
    {
        getTagInternal(key).setShort(getLastNode(key), value);
        return this;
    }

    public short getShort(String key)
    {
        return getShort(key, (short) 0);
    }

    public short getShort(String key, short defaultValue)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.hasKey(actualKey, Tag.SHORT))
            {
                return subTag.getShort(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setDouble(String key, double value)
    {
        getTagInternal(key).setDouble(getLastNode(key), value);
        return this;
    }

    public double getDouble(String key)
    {
        return getDouble(key, 0);
    }

    public double getDouble(String key, double defaultValue)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.hasKey(actualKey, Tag.DOUBLE))
            {
                return subTag.getDouble(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setFloat(String key, float value)
    {
        getTagInternal(key).setFloat(getLastNode(key), value);
        return this;
    }

    public float getFloat(String key)
    {
        return getFloat(key, 0);
    }

    public float getFloat(String key, float defaultValue)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.hasKey(actualKey, Tag.FLOAT))
            {
                return subTag.getFloat(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setByte(String key, byte value)
    {
        getTagInternal(key).setFloat(getLastNode(key), value);
        return this;
    }

    public byte getByte(String key)
    {
        return getByte(key, (byte) 0);
    }

    public byte getByte(String key, byte defaultValue)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.hasKey(actualKey, Tag.BYTE))
            {
                return subTag.getByte(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setBoolean(String key, boolean value)
    {
        getTagInternal(key).setBoolean(getLastNode(key), value);
        return this;
    }

    public boolean getBoolean(String key)
    {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.hasKey(actualKey, Tag.BYTE))
            {
                return subTag.getBoolean(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setPos(String key, BlockPos value)
    {
        getTagInternal(key).setTag(getLastNode(key), NBTUtil.createPosTag(value));
        return this;
    }

    @Nullable
    public BlockPos getPos(String key)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.hasKey(actualKey, Tag.COMPOUND))
            {
                return NBTUtil.getPosFromTag(getTag(actualKey));
            }
        }
        return null;
    }

    public NBTHelper setBlockState(String key, IBlockState value)
    {
        NBTUtil.writeBlockState(getTag(key, true), value);
        return this;
    }

    @Nullable
    public IBlockState getBlockState(String key)
    {
        NBTTagCompound subTag = getTagInternal(key, false, false);
        if (subTag != null)
        {
            return NBTUtil.readBlockState(subTag);
        }
        return null;
    }

    public NBTHelper setGameProfile(String key, GameProfile value)
    {
        NBTUtil.writeGameProfile(getTag(key, true), value);
        return this;
    }

    @Nullable
    public GameProfile getGameProfile(String key)
    {
        NBTTagCompound subTag = getTagInternal(key, false, false);
        if (subTag != null)
        {
            return NBTUtil.readGameProfileFromNBT(subTag);
        }
        return null;
    }

    public NBTHelper setString(String key, String value)
    {
        getTagInternal(key).setString(getLastNode(key), value);
        return this;
    }

    @Nullable
    public String getString(String key)
    {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.hasKey(actualKey, Tag.STRING))
            {
                return subTag.getString(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setIntArray(String key, int[] value)
    {
        getTagInternal(key).setIntArray(getLastNode(key), value);
        return this;
    }

    public int[] getIntArray(String key)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.hasKey(actualKey, Tag.INT_ARRAY))
            {
                return subTag.getIntArray(actualKey);
            }
        }
        return new int[0];
    }

    public NBTHelper setByteArray(String key, byte[] value)
    {
        getTagInternal(key).setByteArray(getLastNode(key), value);
        return this;
    }

    public byte[] getByteArray(String key)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.hasKey(actualKey, Tag.BYTE_ARRAY))
            {
                return subTag.getByteArray(actualKey);
            }
        }
        return new byte[0];
    }

    public NBTHelper setUUID(String key, UUID value)
    {
        getTagInternal(key).setUniqueId(getLastNode(key), value);
        return this;
    }

    @Nullable
    public UUID getUUID(String key)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (!subTag.hasKey(actualKey + "Most", Tag.LONG) || !subTag.hasKey(actualKey + "Least", Tag.LONG))
            {
                return subTag.getUniqueId(actualKey);
            }
        }
        return null;
    }

    public NBTTagList getTagList(String key, int type)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.hasKey(actualKey, Tag.LIST))
            {
                return subTag.getTagList(actualKey, type);
            }
        }
        return null;
    }

    public boolean hasTag(String key, int type)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            return subTag.hasKey(actualKey, type);
        }
        return false;
    }

    // TODO: remove parent if empty?
    public NBTHelper remove(String key)
    {
        NBTTagCompound subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            subTag.removeTag(actualKey);
        }
        return this;
    }

    @Nullable
    public NBTTagCompound get()
    {
        return tag;
    }

    public ItemStack getItem()
    {
        return stack == null ? ItemStack.EMPTY : stack;
    }

    public static NBTHelper of(ItemStack stack)
    {
        return new NBTHelper(stack.getTagCompound(), stack);
    }

    public static NBTHelper of(NBTTagCompound tag)
    {
        return new NBTHelper(tag, null);
    }

    public static NBTHelper create()
    {
        return new NBTHelper(null, null);
    }

}

package snownee.kiwi.util;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class NBTUtil
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

    private NBTUtil(@Nullable NBTTagCompound tag, @Nullable ItemStack stack)
    {
        this.stack = stack;
        this.tag = tag;
    }

    public NBTTagCompound getTag(String key)
    {
        return getTag(key, true);
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

    public NBTUtil setTag(String key, NBTBase value)
    {
        getTagInternal(key).setTag(getLastNode(key), value);
        return this;
    }

    public NBTUtil setInt(String key, int value)
    {
        getTagInternal(key).setInteger(getLastNode(key), value);
        return this;
    }

    public int getInt(String key)
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
        return 0;
    }

    public NBTUtil setLong(String key, long value)
    {
        getTagInternal(key).setLong(getLastNode(key), value);
        return this;
    }

    public long getLong(String key)
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
        return 0;
    }

    public NBTUtil setShort(String key, short value)
    {
        getTagInternal(key).setShort(getLastNode(key), value);
        return this;
    }

    public long getShort(String key)
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
        return 0;
    }

    public NBTUtil setDouble(String key, double value)
    {
        getTagInternal(key).setDouble(getLastNode(key), value);
        return this;
    }

    public double getDouble(String key)
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
        return 0;
    }

    public NBTUtil setFloat(String key, float value)
    {
        getTagInternal(key).setFloat(getLastNode(key), value);
        return this;
    }

    public float getFloat(String key)
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
        return 0;
    }

    public NBTUtil setByte(String key, byte value)
    {
        getTagInternal(key).setFloat(getLastNode(key), value);
        return this;
    }

    public byte getByte(String key)
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
        return 0;
    }

    public NBTUtil setBoolean(String key, boolean value)
    {
        getTagInternal(key).setBoolean(getLastNode(key), value);
        return this;
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

    public NBTUtil setString(String key, String value)
    {
        getTagInternal(key).setString(getLastNode(key), value);
        return this;
    }

    @Nullable
    public String getString(String key)
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
        return null;
    }

    public NBTUtil setIntArray(String key, int[] value)
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

    public NBTUtil setByteArray(String key, byte[] value)
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

    public NBTUtil setUUID(String key, UUID value)
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
    public NBTUtil remove(String key)
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

    public static NBTUtil of(ItemStack stack)
    {
        return new NBTUtil(stack.getTagCompound(), stack);
    }

    public static NBTUtil of(NBTTagCompound tag)
    {
        return new NBTUtil(tag, null);
    }

    public static NBTUtil of()
    {
        return new NBTUtil(null, null);
    }

}

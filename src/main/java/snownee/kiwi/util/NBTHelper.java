package snownee.kiwi.util;

import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * 
 * Simple NBT helper. Use 'a.b.c' to access to values
 * @author Snownee
 *
 */
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
    private CompoundNBT tag;

    private NBTHelper(@Nullable CompoundNBT tag, @Nullable ItemStack stack)
    {
        this.stack = stack;
        this.tag = tag;
    }

    public CompoundNBT getTag(String key)
    {
        return getTag(key, false);
    }

    public CompoundNBT getTag(String key, boolean createIfNull)
    {
        return getTagInternal(key, createIfNull, false);
    }

    private CompoundNBT getTagInternal(String key, boolean createIfNull, boolean ignoreLastNode)
    {
        if (tag == null)
        {
            if (createIfNull)
            {
                tag = new CompoundNBT();
                if (stack != null)
                {
                    stack.setTag(tag);
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
        CompoundNBT subTag = tag;
        String[] parts = key.split("\\.");
        int length = parts.length;
        if (ignoreLastNode)
        {
            --length;
        }
        for (int i = 0; i < length; ++i)
        {
            // TODO: list support. e.g. a.b[2].c.d
            if (!subTag.contains(parts[i], Tag.COMPOUND))
            {
                if (createIfNull)
                {
                    subTag.put(parts[i], new CompoundNBT());
                }
                else
                {
                    return null;
                }
            }
            subTag = (CompoundNBT) subTag.get(parts[i]);
        }
        return subTag;
    }

    private CompoundNBT getTagInternal(String key)
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

    public NBTHelper setTag(String key, INBT value)
    {
        getTagInternal(key).put(getLastNode(key), value);
        return this;
    }

    public NBTHelper setInt(String key, int value)
    {
        getTagInternal(key).putInt(getLastNode(key), value);
        return this;
    }

    public int getInt(String key)
    {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.contains(actualKey, Tag.INT))
            {
                return subTag.getInt(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setLong(String key, long value)
    {
        getTagInternal(key).putLong(getLastNode(key), value);
        return this;
    }

    public long getLong(String key)
    {
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultValue)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.contains(actualKey, Tag.LONG))
            {
                return subTag.getLong(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setShort(String key, short value)
    {
        getTagInternal(key).putShort(getLastNode(key), value);
        return this;
    }

    public short getShort(String key)
    {
        return getShort(key, (short) 0);
    }

    public short getShort(String key, short defaultValue)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.contains(actualKey, Tag.SHORT))
            {
                return subTag.getShort(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setDouble(String key, double value)
    {
        getTagInternal(key).putDouble(getLastNode(key), value);
        return this;
    }

    public double getDouble(String key)
    {
        return getDouble(key, 0);
    }

    public double getDouble(String key, double defaultValue)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.contains(actualKey, Tag.DOUBLE))
            {
                return subTag.getDouble(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setFloat(String key, float value)
    {
        getTagInternal(key).putFloat(getLastNode(key), value);
        return this;
    }

    public float getFloat(String key)
    {
        return getFloat(key, 0);
    }

    public float getFloat(String key, float defaultValue)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.contains(actualKey, Tag.FLOAT))
            {
                return subTag.getFloat(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setByte(String key, byte value)
    {
        getTagInternal(key).putFloat(getLastNode(key), value);
        return this;
    }

    public byte getByte(String key)
    {
        return getByte(key, (byte) 0);
    }

    public byte getByte(String key, byte defaultValue)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.contains(actualKey, Tag.BYTE))
            {
                return subTag.getByte(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setBoolean(String key, boolean value)
    {
        getTagInternal(key).putBoolean(getLastNode(key), value);
        return this;
    }

    public boolean getBoolean(String key)
    {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.contains(actualKey, Tag.BYTE))
            {
                return subTag.getBoolean(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setPos(String key, BlockPos value)
    {
        getTagInternal(key).put(getLastNode(key), NBTUtil.writeBlockPos(value));
        return this;
    }

    @Nullable
    public BlockPos getPos(String key)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.contains(actualKey, Tag.COMPOUND))
            {
                return NBTUtil.readBlockPos(getTag(actualKey));
            }
        }
        return null;
    }

    public NBTHelper setBlockState(String key, BlockState value)
    {
        return setTag(key, NBTUtil.writeBlockState(value));
    }

    @Nullable
    public BlockState getBlockState(String key)
    {
        CompoundNBT subTag = getTagInternal(key, false, false);
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
        CompoundNBT subTag = getTagInternal(key, false, false);
        if (subTag != null)
        {
            return NBTUtil.readGameProfile(subTag);
        }
        return null;
    }

    public NBTHelper setString(String key, String value)
    {
        getTagInternal(key).putString(getLastNode(key), value);
        return this;
    }

    @Nullable
    public String getString(String key)
    {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.contains(actualKey, Tag.STRING))
            {
                return subTag.getString(actualKey);
            }
        }
        return defaultValue;
    }

    public NBTHelper setIntArray(String key, int[] value)
    {
        getTagInternal(key).putIntArray(getLastNode(key), value);
        return this;
    }

    public int[] getIntArray(String key)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.contains(actualKey, Tag.INT_ARRAY))
            {
                return subTag.getIntArray(actualKey);
            }
        }
        return new int[0];
    }

    public NBTHelper setByteArray(String key, byte[] value)
    {
        getTagInternal(key).putByteArray(getLastNode(key), value);
        return this;
    }

    public byte[] getByteArray(String key)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.contains(actualKey, Tag.BYTE_ARRAY))
            {
                return subTag.getByteArray(actualKey);
            }
        }
        return new byte[0];
    }

    public NBTHelper setUUID(String key, UUID value)
    {
        getTagInternal(key).putUniqueId(getLastNode(key), value);
        return this;
    }

    @Nullable
    public UUID getUUID(String key)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (!subTag.contains(actualKey + "Most", Tag.LONG) || !subTag.contains(actualKey + "Least", Tag.LONG))
            {
                return subTag.getUniqueId(actualKey);
            }
        }
        return null;
    }

    public ListNBT getTagList(String key, int type)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            if (subTag.contains(actualKey, Tag.LIST))
            {
                return subTag.getList(actualKey, type);
            }
        }
        return null;
    }

    public boolean hasTag(String key, int type)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            return subTag.contains(actualKey, type);
        }
        return false;
    }

    // TODO: remove parent if empty?
    public NBTHelper remove(String key)
    {
        CompoundNBT subTag = getTagInternal(key, false, true);
        if (subTag != null)
        {
            String actualKey = getLastNode(key);
            subTag.contains(actualKey);
        }
        return this;
    }

    @Nullable
    public CompoundNBT get()
    {
        return tag;
    }

    public ItemStack getItem()
    {
        return stack == null ? ItemStack.EMPTY : stack;
    }

    public static NBTHelper of(ItemStack stack)
    {
        return new NBTHelper(stack.getTag(), stack);
    }

    public static NBTHelper of(CompoundNBT tag)
    {
        return new NBTHelper(tag, null);
    }

    public static NBTHelper create()
    {
        return new NBTHelper(null, null);
    }

}

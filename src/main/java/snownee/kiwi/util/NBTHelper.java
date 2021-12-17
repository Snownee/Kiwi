package snownee.kiwi.util;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 *
 * Simple NBT helper. Use 'a.b.c' to access to values
 *
 * @author Snownee
 *
 */
public class NBTHelper {

	@Nullable
	private ItemStack stack;
	@Nullable
	private CompoundTag tag;

	private NBTHelper(@Nullable CompoundTag tag, @Nullable ItemStack stack) {
		this.stack = stack;
		this.tag = tag;
	}

	@Nullable
	public CompoundTag getTag(String key) {
		return getTag(key, false);
	}

	public CompoundTag getTag(String key, boolean createIfNull) {
		return getTagInternal(key, createIfNull, false);
	}

	private CompoundTag getTagInternal(String key, boolean createIfNull, boolean ignoreLastNode) {
		if (tag == null) {
			if (createIfNull) {
				tag = new CompoundTag();
				if (stack != null) {
					stack.setTag(tag);
				}
			} else {
				return null;
			}
		}
		if (key.isEmpty()) {
			return tag;
		}
		CompoundTag subTag = tag;
		String[] parts = key.split("\\.");
		int length = parts.length;
		if (ignoreLastNode) {
			--length;
		}
		for (int i = 0; i < length; ++i) {
			// TODO: list support. e.g. a.b[2].c.d
			if (!subTag.contains(parts[i], Tag.TAG_COMPOUND)) {
				if (createIfNull) {
					subTag.put(parts[i], new CompoundTag());
				} else {
					return null;
				}
			}
			subTag = (CompoundTag) subTag.get(parts[i]);
		}
		return subTag;
	}

	private CompoundTag getTagInternal(String key) {
		return getTagInternal(key, true, true);
	}

	private String getLastNode(String key) {
		int index = key.lastIndexOf(".");
		if (index < 0) {
			return key;
		} else {
			return key.substring(index + 1);
		}
	}

	public NBTHelper setTag(String key, Tag value) {
		getTagInternal(key).put(getLastNode(key), value);
		return this;
	}

	public NBTHelper setInt(String key, int value) {
		getTagInternal(key).putInt(getLastNode(key), value);
		return this;
	}

	public int getInt(String key) {
		return getInt(key, 0);
	}

	public int getInt(String key, int defaultValue) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			if (subTag.contains(actualKey, Tag.TAG_INT)) {
				return subTag.getInt(actualKey);
			}
		}
		return defaultValue;
	}

	public NBTHelper setLong(String key, long value) {
		getTagInternal(key).putLong(getLastNode(key), value);
		return this;
	}

	public long getLong(String key) {
		return getLong(key, 0);
	}

	public long getLong(String key, long defaultValue) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			if (subTag.contains(actualKey, Tag.TAG_LONG)) {
				return subTag.getLong(actualKey);
			}
		}
		return defaultValue;
	}

	public NBTHelper setShort(String key, short value) {
		getTagInternal(key).putShort(getLastNode(key), value);
		return this;
	}

	public short getShort(String key) {
		return getShort(key, (short) 0);
	}

	public short getShort(String key, short defaultValue) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			if (subTag.contains(actualKey, Tag.TAG_SHORT)) {
				return subTag.getShort(actualKey);
			}
		}
		return defaultValue;
	}

	public NBTHelper setDouble(String key, double value) {
		getTagInternal(key).putDouble(getLastNode(key), value);
		return this;
	}

	public double getDouble(String key) {
		return getDouble(key, 0);
	}

	public double getDouble(String key, double defaultValue) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			if (subTag.contains(actualKey, Tag.TAG_DOUBLE)) {
				return subTag.getDouble(actualKey);
			}
		}
		return defaultValue;
	}

	public NBTHelper setFloat(String key, float value) {
		getTagInternal(key).putFloat(getLastNode(key), value);
		return this;
	}

	public float getFloat(String key) {
		return getFloat(key, 0);
	}

	public float getFloat(String key, float defaultValue) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			if (subTag.contains(actualKey, Tag.TAG_FLOAT)) {
				return subTag.getFloat(actualKey);
			}
		}
		return defaultValue;
	}

	public NBTHelper setByte(String key, byte value) {
		getTagInternal(key).putFloat(getLastNode(key), value);
		return this;
	}

	public byte getByte(String key) {
		return getByte(key, (byte) 0);
	}

	public byte getByte(String key, byte defaultValue) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			if (subTag.contains(actualKey, Tag.TAG_BYTE)) {
				return subTag.getByte(actualKey);
			}
		}
		return defaultValue;
	}

	public NBTHelper setBoolean(String key, boolean value) {
		getTagInternal(key).putBoolean(getLastNode(key), value);
		return this;
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			if (subTag.contains(actualKey, Tag.TAG_BYTE)) {
				return subTag.getBoolean(actualKey);
			}
		}
		return defaultValue;
	}

	public NBTHelper setPos(String key, BlockPos value) {
		getTagInternal(key).put(getLastNode(key), NbtUtils.writeBlockPos(value));
		return this;
	}

	@Nullable
	public BlockPos getPos(String key) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			if (subTag.contains(actualKey, Tag.TAG_COMPOUND)) {
				return NbtUtils.readBlockPos(getTag(actualKey));
			}
		}
		return null;
	}

	//    public NBTHelper setGlobalPos(String key, GlobalPos value) {
	//        getTagInternal(key).put(getLastNode(key), GlobalPos.field_239645_a_.encodeStart(NBTDynamicOps.INSTANCE, value).getOrThrow(allowPartial, onError));
	//        return this;
	//    }
	//
	//    @Nullable
	//    public GlobalPos getGlobalPos(String key) {
	//        CompoundTag subTag = getTagInternal(key, false, true);
	//        if (subTag != null) {
	//            String actualKey = getLastNode(key);
	//            if (subTag.contains(actualKey, NBT.COMPOUND)) {
	//                return GlobalPos.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, getTag(actualKey)));
	//            }
	//        }
	//        return null;
	//    }

	public NBTHelper setBlockState(String key, BlockState value) {
		return setTag(key, NbtUtils.writeBlockState(value));
	}

	public BlockState getBlockState(String key) {
		CompoundTag subTag = getTagInternal(key, false, false);
		if (subTag != null) {
			return NbtUtils.readBlockState(subTag);
		}
		return Blocks.AIR.defaultBlockState();
	}

	public NBTHelper setGameProfile(String key, GameProfile value) {
		NbtUtils.writeGameProfile(getTag(key, true), value);
		return this;
	}

	@Nullable
	public GameProfile getGameProfile(String key) {
		CompoundTag subTag = getTagInternal(key, false, false);
		if (subTag != null) {
			return NbtUtils.readGameProfile(subTag);
		}
		return null;
	}

	public NBTHelper setString(String key, String value) {
		getTagInternal(key).putString(getLastNode(key), value);
		return this;
	}

	@Nullable
	public String getString(String key) {
		return getString(key, null);
	}

	public String getString(String key, String defaultValue) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			if (subTag.contains(actualKey, Tag.TAG_STRING)) {
				return subTag.getString(actualKey);
			}
		}
		return defaultValue;
	}

	public NBTHelper setIntArray(String key, int[] value) {
		getTagInternal(key).putIntArray(getLastNode(key), value);
		return this;
	}

	public int[] getIntArray(String key) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			if (subTag.contains(actualKey, Tag.TAG_INT_ARRAY)) {
				return subTag.getIntArray(actualKey);
			}
		}
		return new int[0];
	}

	public NBTHelper setByteArray(String key, byte[] value) {
		getTagInternal(key).putByteArray(getLastNode(key), value);
		return this;
	}

	public byte[] getByteArray(String key) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			if (subTag.contains(actualKey, Tag.TAG_BYTE_ARRAY)) {
				return subTag.getByteArray(actualKey);
			}
		}
		return new byte[0];
	}

	public NBTHelper setUUID(String key, UUID value) {
		getTagInternal(key).putUUID(getLastNode(key), value);
		return this;
	}

	@Nullable
	public UUID getUUID(String key) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			if (!subTag.contains(actualKey + "Most", Tag.TAG_LONG) || !subTag.contains(actualKey + "Least", Tag.TAG_LONG)) {
				return subTag.getUUID(actualKey);
			}
		}
		return null;
	}

	public ListTag getTagList(String key, int type) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			if (subTag.contains(actualKey, Tag.TAG_LIST)) {
				return subTag.getList(actualKey, type);
			}
		}
		return null;
	}

	public boolean hasTag(String key, int type) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			if (key.isEmpty()) {
				return true;
			}
			String actualKey = getLastNode(key);
			return subTag.contains(actualKey, type);
		}
		return false;
	}

	public Set<String> keySet(String key) {
		return hasTag(key, Tag.TAG_COMPOUND) ? getTag(key).getAllKeys() : Collections.EMPTY_SET;
	}

	// TODO: remove parent if empty?
	public NBTHelper remove(String key) {
		CompoundTag subTag = getTagInternal(key, false, true);
		if (subTag != null) {
			String actualKey = getLastNode(key);
			subTag.remove(actualKey);
		}
		return this;
	}

	@Nullable
	public CompoundTag get() {
		return tag;
	}

	public ItemStack getItem() {
		return stack == null ? ItemStack.EMPTY : stack;
	}

	public static NBTHelper of(ItemStack stack) {
		return new NBTHelper(stack.getTag(), stack);
	}

	public static NBTHelper of(CompoundTag tag) {
		return new NBTHelper(tag, null);
	}

	public static NBTHelper create() {
		return of(new CompoundTag());
	}

}

package snownee.kiwi.block.entity;

import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import snownee.kiwi.block.def.BlockDefinition;
import snownee.kiwi.block.def.SimpleBlockDefinition;
import snownee.kiwi.client.model.RetextureModel;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.NBTHelper.NBT;

public abstract class RetextureBlockEntity extends BaseBlockEntity {
	@Nullable
	protected Map<String, BlockDefinition> textures;
	/** Do not get modelData directly, use getModelData() */
	protected IModelData modelData = EmptyModelData.INSTANCE;

	public RetextureBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos level, BlockState state, String... textureKeys) {
		super(tileEntityTypeIn, level, state);
		persistData = true;
		textures = textureKeys.length == 0 ? null : Maps.newHashMapWithExpectedSize(textureKeys.length);
		for (String key : textureKeys) {
			textures.put(key, null);
		}
	}

	public static void setTexture(Map<String, String> textures, String key, String path) {
		if (!textures.containsKey(key)) {
			return;
		}
		textures.put(key, path);
	}

	public void setTexture(String key, BlockDefinition modelSupplier) {
		if (modelSupplier == null || !isValidTexture(modelSupplier))
			return;
		setTexture(textures, key, modelSupplier);
	}

	public boolean isValidTexture(BlockDefinition modelSupplier) {
		return true;
	}

	public static void setTexture(Map<String, BlockDefinition> textures, String key, BlockDefinition modelSupplier) {
		if (textures == null || !textures.containsKey(key)) {
			return;
		}
		textures.put(key, modelSupplier);
	}

	public static void setTexture(Map<String, BlockDefinition> textures, String key, Item item) {
		Block block = Block.byItem(item);
		if (block != null) {
			setTexture(textures, key, SimpleBlockDefinition.of(block.defaultBlockState()));
		}
	}

	@Override
	public void refresh() {
		if (level != null && level.isClientSide) {
			requestModelDataUpdate();
		} else {
			super.refresh();
		}
	}

	@Override
	public void onLoad() {
		super.requestModelDataUpdate();
	}

	@Override
	public void requestModelDataUpdate() {
		if (textures == null) {
			return;
		}
		super.requestModelDataUpdate();
		if (!remove && level != null && level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
		}
	}

	@Override
	protected void readPacketData(CompoundTag data) {
		if (!data.contains("Overrides", NBT.COMPOUND)) {
			return;
		}
		boolean shouldRefresh = readTextures(textures, data.getCompound("Overrides"), this::isValidTexture);
		if (shouldRefresh) {
			refresh();
		}
	}

	public static boolean readTextures(Map<String, BlockDefinition> textures, CompoundTag data, Predicate<BlockDefinition> validator) {
		if (textures == null) {
			return false;
		}
		boolean shouldRefresh = false;
		NBTHelper helper = NBTHelper.of(data);
		for (String k : textures.keySet()) {
			CompoundTag v = helper.getTag(k);
			if (v == null)
				continue;
			BlockDefinition supplier = BlockDefinition.fromNBT(v);
			if (supplier != null && !validator.test(supplier))
				continue;
			if (!Objects.equals(textures.get(k), supplier)) {
				shouldRefresh = true;
				textures.put(k, supplier);
			}
		}
		return shouldRefresh;
	}

	@Override
	protected CompoundTag writePacketData(CompoundTag data) {
		writeTextures(textures, data);
		return data;
	}

	public static CompoundTag writeTextures(Map<String, BlockDefinition> textures, CompoundTag data) {
		if (textures != null) {
			NBTHelper tag = NBTHelper.of(data);
			textures.forEach((k, v) -> {
				if (v == null)
					return;
				CompoundTag compound = new CompoundTag();
				v.save(compound);
				compound.putString("Type", v.getFactory().getId());
				tag.setTag("Overrides." + k, compound);
			});
		}
		return data;
	}

	@Override
	public IModelData getModelData() {
		if (textures != null && modelData == EmptyModelData.INSTANCE) {
			modelData = new ModelDataMap.Builder().withInitial(RetextureModel.TEXTURES, textures).build();
		}
		return modelData;
	}

	@OnlyIn(Dist.CLIENT)
	public int getColor(BlockAndTintGetter level, int index) {
		return RetextureModel.getColor(textures, getBlockState(), level, worldPosition, index);
	}
}

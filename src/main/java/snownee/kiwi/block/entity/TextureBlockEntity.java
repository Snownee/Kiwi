package snownee.kiwi.block.entity;

import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.client.model.TextureModel;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.NBTHelper.NBT;
import snownee.kiwi.util.Util;

public class TextureBlockEntity extends BaseBlockEntity {
	@Nullable
	protected Map<String, String> textures;
	@Nullable
	protected Map<String, Item> marks;
	protected IModelData modelData;

	public TextureBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state, String... textureKeys) {
		super(tileEntityTypeIn, pos, state);
		persistData = true;
		textures = textureKeys.length == 0 ? null : Maps.newHashMapWithExpectedSize(textureKeys.length);
		for (String key : textureKeys) {
			textures.put(key, "");
		}
		if (EffectiveSide.get().isClient()) {
			modelData = textures == null ? EmptyModelData.INSTANCE : new ModelDataMap.Builder().withInitial(TextureModel.TEXTURES, textures).build();
		}
	}

	public void setTexture(String key, String path) {
		setTexture(textures, key, path);
	}

	public static void setTexture(Map<String, String> textures, String key, String path) {
		if (!textures.containsKey(key)) {
			return;
		}
		textures.put(key, path);
	}

	public void setTexture(String key, BlockState state) {
		setTexture(textures, key, state, EmptyModelData.INSTANCE);
		if (isMark(key)) {
			Item item = state.getBlock().asItem();
			if (item == Items.AIR) {
				return;
			}
			if (marks == null) {
				marks = Maps.newHashMap();
			}
			marks.put(key, item);
		}
	}

	public static void setTexture(Map<String, String> textures, String key, BlockState state, IModelData modelData) {
		if (textures == null || !textures.containsKey(key)) {
			return;
		}
		if (EffectiveSide.get().isServer()) {
			String value = NbtUtils.writeBlockState(state).toString();
			textures.put(key, value);
		} else {
			textures.put(key, getTextureFromState(state, key, modelData));
		}
	}

	public void setTexture(String key, Item item) {
		setTexture(textures, key, item);
		if (isMark(key)) {
			if (marks == null) {
				marks = Maps.newHashMap();
			}
			marks.put(key, item);
		}
	}

	public static void setTexture(Map<String, String> textures, String key, Item item) {
		Block block = Block.byItem(item);
		if (block != null) {
			setTexture(textures, key, block.defaultBlockState(), EmptyModelData.INSTANCE);
		}
	}

	/**
	 * @since 2.3.0
	 */
	public Item getMark(String key) {
		return marks == null ? Items.AIR : marks.getOrDefault(key, Items.AIR);
	}

	@OnlyIn(Dist.CLIENT)
	public static String getTextureFromState(BlockState state, String key, IModelData modelData) {
		if ("top".equals(key))
			key = "up";
		else if ("bottom".equals(key))
			key = "down";
		return getTextureFromState(state, Direction.byName(key), modelData);
	}

	@OnlyIn(Dist.CLIENT)
	public static String getTextureFromState(BlockState state, Direction direction, IModelData modelData) {
		BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
		Random random = new Random();
		random.setSeed(42L);
		ResourceLocation particleIcon = model.getParticleIcon(modelData).getName();
		ResourceLocation sprite = particleIcon;
		if (direction != null) {
			List<BakedQuad> quads = model.getQuads(state, direction, random, modelData);
			for (BakedQuad quad : quads) {
				sprite = quad.getSprite().getName();
				if (sprite.equals(particleIcon)) {
					break;
				}
			}
		}
		return Util.trimRL(sprite);
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
		setTexture("top", Blocks.DIAMOND_BLOCK.defaultBlockState());
		super.requestModelDataUpdate();
	}

	@Override
	public void requestModelDataUpdate() {
		if (textures == null) {
			return;
		}
		super.requestModelDataUpdate();
		if (level != null && level.isClientSide) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
		}
	}

	@Override
	protected void readPacketData(CompoundTag data) {
		if (!data.contains("Textures", NBT.COMPOUND)) {
			return;
		}
		boolean shouldRefresh = readTextures(textures, data.getCompound("Textures"), EmptyModelData.INSTANCE);
		if (data.contains("Items", NBT.COMPOUND)) {
			NBTHelper helper = NBTHelper.of(data.getCompound("Items"));
			for (String k : helper.keySet("")) {
				if (!isMark(k)) {
					continue;
				}
				if (marks == null) {
					marks = Maps.newHashMap();
				}
				ResourceLocation locator = ResourceLocation.tryParse(helper.getString(k));
				if (locator != null) {
					Item item = ForgeRegistries.ITEMS.getValue(locator);
					if (item != null) {
						marks.put(k, item);
					}
				}
			}
		}
		if (shouldRefresh) {
			refresh();
		}
	}

	public static boolean readTextures(Map<String, String> textures, CompoundTag data, IModelData modelData) {
		if (textures == null) {
			return false;
		}
		boolean shouldRefresh = false;
		NBTHelper helper = NBTHelper.of(data);
		for (String k : textures.keySet()) {
			String v = helper.getString(k);
			if (v == null)
				continue;
			if (EffectiveSide.get().isClient() && v.startsWith("{")) {
				try {
					CompoundTag stateNbt = TagParser.parseTag(v);
					BlockState state = NbtUtils.readBlockState(stateNbt);
					v = getTextureFromState(state, k, modelData);
				} catch (CommandSyntaxException e) {
					continue;
				}
			}
			if (!textures.get(k).equals(v)) {
				shouldRefresh = true;
				textures.put(k, v);
			}
		}
		return shouldRefresh;
	}

	public boolean isMark(String key) {
		return false;
	}

	@Override
	protected CompoundTag writePacketData(CompoundTag data) {
		writeTextures(textures, data);
		if (marks != null) {
			NBTHelper helper = NBTHelper.of(data);
			marks.forEach((k, v) -> {
				if (isMark(k))
					helper.setString("Items." + k, Util.trimRL(v.getRegistryName()));
			});
		}
		return data;
	}

	public static CompoundTag writeTextures(Map<String, String> textures, CompoundTag data) {
		if (textures != null) {
			NBTHelper tag = NBTHelper.of(data);
			textures.forEach((k, v) -> tag.setString("Textures." + k, v));
		}
		return data;
	}

	@Override
	public IModelData getModelData() {
		return modelData;
	}
}

package snownee.kiwi.client.model;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import snownee.kiwi.block.entity.TextureBlockEntity;
import snownee.kiwi.util.NBTHelper;

@SuppressWarnings("deprecation")
@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class TextureModel implements IDynamicBakedModel {
	public static ModelProperty<Map<String, String>> TEXTURES = new ModelProperty<>();
	public static Map<BakedModel, TextureModel> CACHES = Maps.newHashMap();

	public static void register(ModelBakeEvent event, Block block, @Nullable BlockState inventoryState) {
		register(event, block, inventoryState, null);
	}

	/**
	 * @since 2.3.0
	 */
	public static void register(ModelBakeEvent event, Block block, @Nullable BlockState inventoryState, @Nullable String particleKey) {
		block.getStateDefinition().getPossibleStates().forEach(s -> {
			ModelResourceLocation rl = BlockModelShaper.stateToModelLocation(s);
			register(event, block, rl, s.equals(inventoryState), particleKey);
		});
		CACHES.clear();
	}

	/**
	 * @since 2.3.0
	 */
	public static void register(ModelBakeEvent event, Block block, ModelResourceLocation rl, boolean inventory, @Nullable String particleKey) {
		TextureModel textureModel = process(event, rl, inventory, particleKey);
		if (inventory && textureModel != null && block != null && block.asItem() != null) {
			rl = new ModelResourceLocation(new ResourceLocation(rl.getNamespace(), rl.getPath()), "inventory");
			event.getModelRegistry().put(rl, textureModel);
			ItemModelShaper mesher = Minecraft.getInstance().getItemRenderer().getItemModelShaper();
			mesher.register(block.asItem(), rl);
		}
	}

	/**
	 * @since 2.3.0
	 */
	public static void registerInventory(ModelBakeEvent event, ItemLike item, @Nullable String particleKey) {
		ResourceLocation rl = item.asItem().getRegistryName();
		ModelResourceLocation modelRl = new ModelResourceLocation(new ResourceLocation(rl.getNamespace(), rl.getPath()), "inventory");
		process(event, modelRl, true, particleKey);
		ItemModelShaper mesher = Minecraft.getInstance().getItemRenderer().getItemModelShaper();
		mesher.register(item.asItem(), modelRl);
	}

	@Nullable
	private static TextureModel process(ModelBakeEvent event, ModelResourceLocation rl, boolean inventory, @Nullable String particleKey) {
		UnbakedModel unbakedModel = event.getModelLoader().getModelOrLogError(rl, "Kiwi failed to replace block model " + rl);
		BakedModel bakedModel = event.getModelRegistry().get(rl);
		if (unbakedModel == null || bakedModel == null) {
			return null;
		}
		TextureModel textureModel = null;
		if (unbakedModel instanceof MultiPart && bakedModel instanceof MultiPartBakedModel) {
			MultiPart originalUnbaked = (MultiPart) unbakedModel;
			MultiPartBakedModel originalBaked = (MultiPartBakedModel) bakedModel;
			List<Selector> selectors = originalUnbaked.getSelectors();
			TextureMultipart.Builder builder = new TextureMultipart.Builder();
			for (int i = 0; i < selectors.size(); i++) {
				Selector selector = selectors.get(i);
				MultiVariant variantList = selector.getVariant();
				Pair<Predicate<BlockState>, BakedModel> pair = originalBaked.selectors.get(i);
				builder.putModel(pair.getLeft(), putModel(event, variantList, pair.getRight(), false, particleKey));
			}
			event.getModelRegistry().put(rl, builder.build());
		} else if (unbakedModel instanceof MultiVariant) {
			textureModel = putModel(event, (MultiVariant) unbakedModel, bakedModel, inventory, particleKey);
		} else if (unbakedModel instanceof BlockModel) {
			textureModel = new TextureModel(event.getModelLoader(), (BlockModel) unbakedModel, bakedModel, BlockModelRotation.X0_Y0, inventory, particleKey);
		}
		if (textureModel != null) {
			event.getModelRegistry().put(rl, textureModel);
		}
		return textureModel;
	}

	@Nullable
	private static TextureModel putModel(ModelBakeEvent event, MultiVariant variantList, BakedModel baked, boolean inventory, String particleKey) {
		TextureModel textureModel = null;
		if (CACHES.containsKey(baked)) {
			textureModel = CACHES.get(baked);
			if (inventory) {
				textureModel.setOverrides();
			}
		} else {
			List<Variant> variants = variantList.getVariants();
			for (Variant variant : variants) {
				UnbakedModel unbakedModel2 = event.getModelLoader().getModel(variant.getModelLocation());
				if (unbakedModel2 instanceof BlockModel) {
					textureModel = new TextureModel(event.getModelLoader(), (BlockModel) unbakedModel2, baked, variant, inventory, particleKey);
					CACHES.put(baked, textureModel);
					break;
				}
			}
		}
		return textureModel;
	}

	private final ModelLoader modelLoader;
	private final ModelState variant;
	private final BlockModel originalUnbaked;
	private final BakedModel originalBaked;
	private TextureOverrideList overrideList;
	private final String particleKey;
	private final Cache<String, BakedModel> baked = CacheBuilder.newBuilder().expireAfterAccess(500L, TimeUnit.SECONDS).build();

	public TextureModel(ModelLoader modelLoader, BlockModel originalUnbaked, BakedModel originalBaked, ModelState variant, boolean inventory, @Nullable String particleKey) {
		this.modelLoader = modelLoader;
		this.originalUnbaked = originalUnbaked;
		this.originalBaked = originalBaked;
		this.variant = variant;
		this.particleKey = particleKey;
		overrideList = inventory ? new TextureOverrideList(this) : null;
	}

	public void setOverrides() {
		if (overrideList == null) {
			overrideList = new TextureOverrideList(this);
		}
	}

	@Override
	public boolean useAmbientOcclusion() {
		return originalBaked.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return originalBaked.isGui3d();
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleIcon(IModelData data) {
		if (particleKey != null && data.getData(TEXTURES) != null && data.getData(TEXTURES).containsKey(particleKey)) {
			Material material = ModelLoaderRegistry.blockMaterial(data.getData(TEXTURES).get(particleKey));
			TextureAtlasSprite particle = ModelLoader.defaultTextureGetter().apply(material);
			if (particle.getClass() != MissingTextureAtlasSprite.class) {
				return particle;
			}
		}
		return getParticleIcon();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return originalBaked.getParticleIcon();
	}

	@Override
	public ItemOverrides getOverrides() {
		return overrideList == null ? originalBaked.getOverrides() : overrideList;
	}

	@Override
	public ItemTransforms getTransforms() {
		return originalBaked.getTransforms();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
		Map<String, String> overrides = extraData.getData(TEXTURES);
		BakedModel model = getModel(overrides);
		return model.getQuads(state, side, rand);
	}

	private BakedModel getModel(Map<String, String> overrides) {
		if (overrides == null) {
			return originalBaked;
		}
		Map<String, Either<Material, String>> textures = Maps.newHashMap();
		resolveTextures(textures, originalUnbaked, false);
		overrides.forEach((k, v) -> {
			if (!v.isEmpty())
				textures.put(k, Either.left(ModelLoaderRegistry.blockMaterial(v)));
		});
		String key = generateKey(overrides);
		BakedModel model = null;
		ResourceLocation loaderId = new ResourceLocation("minecraft:elements");
		try {
			model = baked.get(key, () -> {
				BlockModel unbaked = new BlockModel(originalUnbaked.getParentLocation(), originalUnbaked.getElements(), textures, originalUnbaked.hasAmbientOcclusion(), originalUnbaked.getGuiLight(), originalUnbaked.getTransforms(), Lists.newArrayList(originalUnbaked.getOverrides()));
				return unbaked.bake(modelLoader, unbaked, ModelLoader.defaultTextureGetter(), variant, loaderId, true);
			});
		} catch (ExecutionException e) {
			e.printStackTrace();
			return originalBaked;
		}
		return model;
	}

	private static void resolveTextures(Map<String, Either<Material, String>> textures2, BlockModel model, boolean sub) {
		if (model.parent != null) {
			resolveTextures(textures2, model.parent, true);
		}
		textures2.putAll(model.textureMap);
		if (!sub) {
			Set<String> hashes = Sets.newHashSet();
			textures2.forEach((k, v) -> {
				v.ifRight(s -> {
					hashes.add(k);
				});
			});
			do {
				hashes.removeIf(k -> {
					Either<Material, String> v = textures2.get(k);
					if (v == null) {
						textures2.put(k, Either.left(ModelLoaderRegistry.blockMaterial(MissingTextureAtlasSprite.getLocation())));
						return true;
					}
					String hash = v.right().get();
					Either<Material, String> to = textures2.get(hash);
					if (to == null || hash.equals(k) || to.right().isPresent() && to.right().get().equals(k)) {
						textures2.put(k, Either.left(ModelLoaderRegistry.blockMaterial(MissingTextureAtlasSprite.getLocation())));
						return true;
					} else {
						textures2.put(k, to);
						return to.left().isPresent();
					}
				});
			} while (!hashes.isEmpty());
		}
	}

	@Override
	public IModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, IModelData tileData) {
		return tileData;
	}

	private static String generateKey(Map<String, String> textures) {
		if (textures == null) {
			return "";
		} else {
			return StringUtils.join(textures.entrySet(), ',');
		}
	}

	public static class TextureOverrideList extends ItemOverrides {
		private final TextureModel baked;
		private final Cache<ItemStack, BakedModel> cache = CacheBuilder.newBuilder().maximumSize(100L).expireAfterWrite(300L, TimeUnit.SECONDS).weakKeys().build();

		public TextureOverrideList(TextureModel model) {
			baked = model;
		}

		@Override
		public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel worldIn, LivingEntity entityIn, int p_173469_) {
			if (model instanceof TextureModel) {
				try {
					model = cache.get(stack, () -> {
						CompoundTag data = NBTHelper.of(stack).getTag("BlockEntityTag.Textures");
						if (data == null) {
							return baked.originalBaked;
						}
						Set<String> keySet = data.getAllKeys();
						Map<String, String> overrides = Maps.newHashMapWithExpectedSize(keySet.size());
						keySet.forEach(k -> overrides.put(k, ""));
						TextureBlockEntity.readTextures(overrides, data);
						return baked.getModel(overrides);
					});
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			return model;
		}

		@Override
		public ImmutableList<BakedOverride> getOverrides() {
			return ImmutableList.of();
		}
	}

	@Override
	public boolean usesBlockLight() {
		return originalBaked.usesBlockLight();
	}
}

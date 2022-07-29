package snownee.kiwi.client.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.BlockGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import snownee.kiwi.block.def.BlockDefinition;
import snownee.kiwi.block.entity.RetextureBlockEntity;
import snownee.kiwi.util.NBTHelper;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class RetextureModel implements IDynamicBakedModel {
	public static ModelProperty<Map<String, BlockDefinition>> TEXTURES = new ModelProperty<>();

	public static class ModelConfiguration implements IGeometryBakingContext {

		private final IGeometryBakingContext baseConfiguration;
		private final Map<String, BlockDefinition> overrides;

		public ModelConfiguration(IGeometryBakingContext baseConfiguration, Map<String, BlockDefinition> overrides) {
			this.baseConfiguration = baseConfiguration;
			this.overrides = overrides;
		}

		@Override
		public String getModelName() {
			return baseConfiguration.getModelName();
		}

		@Override
		public boolean hasMaterial(String name) {
			return baseConfiguration.hasMaterial(name);
		}

		@Override
		public Material getMaterial(String name) {
			if (name.charAt(0) == '#') {
				String ref = name.substring(1);
				int i = ref.lastIndexOf('_');
				if (i != -1) {
					String ref0 = ref.substring(0, i);
					BlockDefinition supplier = overrides.get(ref0);
					if (supplier != null) {
						Direction direction = Direction.byName(ref.substring(i + 1));
						return supplier.renderMaterial(direction);
					}
				}
				BlockDefinition supplier = overrides.get(ref);
				if (supplier != null) {
					return supplier.renderMaterial(null);
				}
			}
			return baseConfiguration.getMaterial(name);
		}

		@Override
		public boolean isGui3d() {
			return baseConfiguration.isGui3d();
		}

		@Override
		public boolean useBlockLight() {
			return baseConfiguration.useBlockLight();
		}

		@Override
		public boolean useAmbientOcclusion() {
			return baseConfiguration.useAmbientOcclusion();
		}

		@Override
		public ItemTransforms getTransforms() {
			return baseConfiguration.getTransforms();
		}

		@Override
		public Transformation getRootTransform() {
			return baseConfiguration.getRootTransform();
		}

		@Override
		public @Nullable ResourceLocation getRenderTypeHint() {
			//ChunkRenderTypeSet.union(overrides.values().stream().map(BlockDefinition::getRenderTypes).toList());
			return null;
		}

		@Override
		public boolean isComponentVisible(String component, boolean fallback) {
			return baseConfiguration.isComponentVisible(component, fallback);
		}

	}

	public static class Geometry implements IUnbakedGeometry<Geometry> {

		private final ResourceLocation loaderId;
		private final Function<ModelBakery, BlockModel> blockModel;
		private final String particle;
		private final boolean inventory;

		public Geometry(Function<ModelBakery, BlockModel> blockModel, ResourceLocation loaderId, String particle, boolean inventory) {
			this.blockModel = blockModel;
			this.loaderId = loaderId;
			this.particle = particle;
			this.inventory = inventory;
		}

		@Override
		public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
			return new RetextureModel(bakery, modelTransform, loaderId, blockModel.apply(bakery).customData, particle, inventory);
		}

		@Override
		public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<com.mojang.datafixers.util.Pair<String, String>> missingTextureErrors) {
			return Collections.EMPTY_LIST;
		}

	}

	public static class Loader implements IGeometryLoader<Geometry> {

		public static final Loader INSTANCE = new Loader();

		private Loader() {
		}

		@Override
		public Geometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
			ResourceLocation loaderId = new ResourceLocation(GsonHelper.getAsString(jsonObject, "base_loader", "elements"));
			Function<ModelBakery, BlockModel> blockModel = bakery -> (BlockModel) bakery.getModel(new ResourceLocation(GsonHelper.getAsString(jsonObject, "base")));
			return new Geometry(blockModel, loaderId, GsonHelper.getAsString(jsonObject, "particle", "0"), GsonHelper.getAsBoolean(jsonObject, "inventory", true));
		}

	}

	private final ModelBakery modelLoader;
	private final ModelState variant;
	private final ResourceLocation loaderId;
	private ItemOverrides overrideList;
	private final Cache<String, BakedModel> baked = CacheBuilder.newBuilder().expireAfterAccess(500L, TimeUnit.SECONDS).build();
	private final BlockGeometryBakingContext baseConfiguration;
	private final String particleKey;

	public RetextureModel(ModelBakery modelLoader, ModelState variant, ResourceLocation loaderId, BlockGeometryBakingContext baseConfiguration, String particleKey, boolean inventory) {
		this.modelLoader = modelLoader;
		this.variant = variant;
		this.loaderId = loaderId;
		this.baseConfiguration = baseConfiguration;
		overrideList = inventory ? new Overrides(this) : ItemOverrides.EMPTY;
		this.particleKey = particleKey;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return baseConfiguration.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return baseConfiguration.isGui3d();
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleIcon(ModelData data) {
		if (data.get(TEXTURES) != null) {
			BlockDefinition supplier = data.get(TEXTURES).get(particleKey);
			if (supplier != null) {
				Material material = supplier.renderMaterial(null);
				TextureAtlasSprite particle = modelLoader.getAtlasSet().getSprite(material);
				if (particle.getClass() != MissingTextureAtlasSprite.class) {
					return particle;
				}
			}
		}
		return getParticleIcon();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return modelLoader.getAtlasSet().getSprite(baseConfiguration.getMaterial("particle"));
	}

	@Override
	public ItemOverrides getOverrides() {
		return overrideList;
	}

	@Override
	public ItemTransforms getTransforms() {
		return baseConfiguration.getTransforms();
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
		Map<String, BlockDefinition> overrides = extraData.get(TEXTURES);
		if (overrides == null)
			overrides = Collections.EMPTY_MAP;
		boolean noSupplier = true;
		if (renderType != null) {
			for (BlockDefinition supplier : overrides.values()) {
				if (supplier != null) {
					noSupplier = false;
					if (supplier.canRenderInLayer(renderType)) {
						BakedModel model = getModel(overrides);
						return model.getQuads(state, side, rand, extraData, renderType);
					}
				}
			}
		}
		if (renderType == null || (noSupplier && renderType == RenderType.solid())) {
			BakedModel model = getModel(overrides);
			return model.getQuads(state, side, rand, extraData, renderType);
		}
		return Collections.EMPTY_LIST;
	}

	public BakedModel getModel(Map<String, BlockDefinition> overrides) {
		String key = generateKey(overrides);
		try {
			return baked.get(key, () -> {
				ModelConfiguration configuration = new ModelConfiguration(baseConfiguration, overrides);
				return baseConfiguration.getCustomGeometry().bake(configuration, modelLoader, modelLoader.getAtlasSet()::getSprite, variant, overrideList, loaderId);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Minecraft.getInstance().getModelManager().getMissingModel();
	}

	private static String generateKey(Map<String, BlockDefinition> overrides) {
		if (overrides == null) {
			return "";
		} else {
			return StringUtils.join(overrides.entrySet(), ',');
		}
	}

	public static class Overrides extends ItemOverrides {
		private final RetextureModel baked;
		private final Cache<ItemStack, BakedModel> cache = CacheBuilder.newBuilder().maximumSize(100L).expireAfterWrite(300L, TimeUnit.SECONDS).weakKeys().build();

		public Overrides(RetextureModel model) {
			baked = model;
		}

		@Override
		public BakedModel resolve(BakedModel model, ItemStack stack, ClientLevel worldIn, LivingEntity entityIn, int seed) {
			if (model instanceof RetextureModel) {
				try {
					model = cache.get(stack, () -> {
						return baked.getModel(overridesFromItem(stack));
					});
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			return model;
		}

		public static Map<String, BlockDefinition> overridesFromItem(ItemStack stack) {
			CompoundTag data = NBTHelper.of(stack).getTag("BlockEntityTag.Overrides");
			if (data == null)
				data = new CompoundTag();
			Set<String> keySet = data.getAllKeys();
			Map<String, BlockDefinition> overrides = Maps.newHashMapWithExpectedSize(keySet.size());
			keySet.forEach(k -> overrides.put(k, null));
			RetextureBlockEntity.readTextures(overrides, data, Predicates.alwaysTrue());
			return overrides;
		}

		@Override
		public ImmutableList<BakedOverride> getOverrides() {
			return ImmutableList.of();
		}
	}

	@Override
	public boolean usesBlockLight() {
		return baseConfiguration.useBlockLight();
	}

	public static int getColor(Map<String, BlockDefinition> textures, BlockState state, BlockAndTintGetter level, BlockPos pos, int index) {
		BlockDefinition supplier = textures.get(Integer.toString(index));
		if (supplier != null)
			return supplier.getColor(state, level, pos, index);
		return -1;
	}
}
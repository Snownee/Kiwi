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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverride;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.renderer.model.MultipartBakedModel;
import net.minecraft.client.renderer.model.Variant;
import net.minecraft.client.renderer.model.VariantList;
import net.minecraft.client.renderer.model.multipart.Multipart;
import net.minecraft.client.renderer.model.multipart.Selector;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraft.world.World;
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
import snownee.kiwi.tile.TextureTile;
import snownee.kiwi.util.NBTHelper;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class TextureModel implements IDynamicBakedModel
{
    public static ModelProperty<Map<String, String>> TEXTURES = new ModelProperty<>();
    public static Map<IBakedModel, TextureModel> CACHES = Maps.newHashMap();

    public static void register(ModelBakeEvent event, Block block, @Nullable BlockState inventoryState)
    {
        register(event, block, inventoryState, null);
    }

    /**
     * @since 2.3.0
     */
    public static void register(ModelBakeEvent event, Block block, @Nullable BlockState inventoryState, @Nullable String particleKey)
    {
        block.getStateContainer().getValidStates().forEach(s -> {
            ModelResourceLocation rl = BlockModelShapes.getModelLocation(s);
            register(event, block, rl, s.equals(inventoryState), particleKey);
        });
        CACHES.clear();
    }

    /**
     * @since 2.3.0
     */
    public static void register(ModelBakeEvent event, Block block, ModelResourceLocation rl, boolean inventory, @Nullable String particleKey)
    {
        TextureModel textureModel = process(event, rl, inventory, particleKey);
        if (inventory && textureModel != null && block != null && block.asItem() != null)
        {
            rl = new ModelResourceLocation(new ResourceLocation(rl.getNamespace(), rl.getPath()), "inventory");
            event.getModelRegistry().put(rl, textureModel);
            ItemModelMesher mesher = Minecraft.getInstance().getItemRenderer().getItemModelMesher();
            mesher.register(block.asItem(), rl);
        }
    }

    /**
     * @since 2.3.0
     */
    public static void registerInventory(ModelBakeEvent event, IItemProvider item, @Nullable String particleKey)
    {
        ResourceLocation rl = item.asItem().getRegistryName();
        ModelResourceLocation modelRl = new ModelResourceLocation(new ResourceLocation(rl.getNamespace(), rl.getPath()), "inventory");
        process(event, modelRl, true, particleKey);
        ItemModelMesher mesher = Minecraft.getInstance().getItemRenderer().getItemModelMesher();
        mesher.register(item.asItem(), modelRl);
    }

    @Nullable
    private static TextureModel process(ModelBakeEvent event, ModelResourceLocation rl, boolean inventory, @Nullable String particleKey)
    {
        IUnbakedModel unbakedModel = event.getModelLoader().getModelOrLogError(rl, "Kiwi failed to replace block model " + rl);
        IBakedModel bakedModel = event.getModelRegistry().get(rl);
        if (unbakedModel == null || bakedModel == null)
        {
            return null;
        }
        TextureModel textureModel = null;
        if (unbakedModel instanceof Multipart && bakedModel instanceof MultipartBakedModel)
        {
            Multipart originalUnbaked = (Multipart) unbakedModel;
            MultipartBakedModel originalBaked = (MultipartBakedModel) bakedModel;
            List<Selector> selectors = originalUnbaked.getSelectors();
            TextureMultipart.Builder builder = new TextureMultipart.Builder();
            for (int i = 0; i < selectors.size(); i++)
            {
                Selector selector = selectors.get(i);
                VariantList variantList = selector.getVariantList();
                Pair<Predicate<BlockState>, IBakedModel> pair = originalBaked.selectors.get(i);
                builder.putModel(pair.getLeft(), putModel(event, variantList, pair.getRight(), false, particleKey));
            }
            event.getModelRegistry().put(rl, builder.build());
        }
        else if (unbakedModel instanceof VariantList)
        {
            textureModel = putModel(event, (VariantList) unbakedModel, bakedModel, inventory, particleKey);
        }
        else if (unbakedModel instanceof BlockModel)
        {
            textureModel = new TextureModel(event.getModelLoader(), (BlockModel) unbakedModel, bakedModel, ModelRotation.X0_Y0, inventory, particleKey);
        }
        if (textureModel != null)
        {
            event.getModelRegistry().put(rl, textureModel);
        }
        return textureModel;
    }

    @Nullable
    private static TextureModel putModel(ModelBakeEvent event, VariantList variantList, IBakedModel baked, boolean inventory, String particleKey)
    {
        TextureModel textureModel = null;
        if (CACHES.containsKey(baked))
        {
            textureModel = CACHES.get(baked);
            if (inventory)
            {
                textureModel.setOverrides();
            }
        }
        else
        {
            List<Variant> variants = variantList.getVariantList();
            for (Variant variant : variants)
            {
                IUnbakedModel unbakedModel2 = event.getModelLoader().getUnbakedModel(variant.getModelLocation());
                if (unbakedModel2 instanceof BlockModel)
                {
                    textureModel = new TextureModel(event.getModelLoader(), (BlockModel) unbakedModel2, baked, variant, inventory, particleKey);
                    CACHES.put(baked, textureModel);
                    break;
                }
            }
        }
        return textureModel;
    }

    private final ModelLoader modelLoader;
    private final IModelTransform variant;
    private final BlockModel originalUnbaked;
    private final IBakedModel originalBaked;
    private TextureOverrideList overrideList;
    private final String particleKey;
    private final Cache<String, IBakedModel> baked = CacheBuilder.newBuilder().maximumSize(200L).expireAfterWrite(500L, TimeUnit.SECONDS).weakKeys().build();

    public TextureModel(ModelLoader modelLoader, BlockModel originalUnbaked, IBakedModel originalBaked, IModelTransform variant, boolean inventory, @Nullable String particleKey)
    {
        this.modelLoader = modelLoader;
        this.originalUnbaked = originalUnbaked;
        this.originalBaked = originalBaked;
        this.variant = variant;
        this.particleKey = particleKey;
        overrideList = inventory ? new TextureOverrideList(this) : null;
    }

    public void setOverrides()
    {
        if (overrideList == null)
        {
            overrideList = new TextureOverrideList(this);
        }
    }

    @Override
    public boolean isAmbientOcclusion()
    {
        return originalBaked.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d()
    {
        return originalBaked.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer()
    {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IModelData data)
    {
        if (particleKey != null && data.getData(TEXTURES) != null && data.getData(TEXTURES).containsKey(particleKey))
        {
            Material material = ModelLoaderRegistry.blockMaterial(data.getData(TEXTURES).get(particleKey));
            TextureAtlasSprite particle = ModelLoader.defaultTextureGetter().apply(material);
            if (particle.getClass() != MissingTextureSprite.class)
            {
                return particle;
            }
        }
        return getParticleTexture();
    }

    @Override
    @SuppressWarnings("deprecation")
    public TextureAtlasSprite getParticleTexture()
    {
        return originalBaked.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides()
    {
        return overrideList == null ? originalBaked.getOverrides() : overrideList;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return originalBaked.getItemCameraTransforms();
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData)
    {
        Map<String, String> overrides = extraData.getData(TEXTURES);
        IBakedModel model = getModel(overrides);
        return model.getQuads(state, side, rand);
    }

    private IBakedModel getModel(Map<String, String> overrides)
    {
        if (overrides == null)
        {
            return originalBaked;
        }
        Map<String, Either<Material, String>> textures = Maps.newHashMap();
        resolveTextures(textures, originalUnbaked, false);
        overrides.forEach((k, v) -> {
            if (!v.isEmpty())
                textures.put(k, Either.left(ModelLoaderRegistry.blockMaterial(v)));
        });
        String key = generateKey(overrides);
        IBakedModel model = null;
        ResourceLocation loaderId = new ResourceLocation("minecraft:elements");
        try
        {
            model = baked.get(key, () -> {
                BlockModel unbaked = new BlockModel(originalUnbaked.getParentLocation(), originalUnbaked.getElements(), textures, originalUnbaked.isAmbientOcclusion(), originalUnbaked.func_230176_c_(), originalUnbaked.getAllTransforms(), Lists.newArrayList(originalUnbaked.getOverrides()));
                return unbaked.bakeModel(modelLoader, ModelLoader.defaultTextureGetter(), variant, loaderId);
            });
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
            return originalBaked;
        }
        return model;
    }

    private static void resolveTextures(Map<String, Either<Material, String>> textures2, BlockModel model, boolean sub)
    {
        if (model.parent != null)
        {
            resolveTextures(textures2, model.parent, true);
        }
        textures2.putAll(model.textures);
        if (!sub)
        {
            Set<String> hashes = Sets.newHashSet();
            textures2.forEach((k, v) -> {
                v.ifRight(s -> {
                    hashes.add(k);
                });
            });
            do
            {
                hashes.removeIf(k -> {
                    Either<Material, String> v = textures2.get(k);
                    if (v == null)
                    {
                        textures2.put(k, Either.left(ModelLoaderRegistry.blockMaterial(MissingTextureSprite.getLocation())));
                        return true;
                    }
                    String hash = v.right().get();
                    Either<Material, String> to = textures2.get(hash);
                    if (to == null || hash.equals(k) || to.right().isPresent() && to.right().get().equals(k))
                    {
                        textures2.put(k, Either.left(ModelLoaderRegistry.blockMaterial(MissingTextureSprite.getLocation())));
                        return true;
                    }
                    else
                    {
                        textures2.put(k, to);
                        return to.left().isPresent();
                    }
                });
            }
            while (!hashes.isEmpty());
        }
    }

    @Override
    public IModelData getModelData(ILightReader world, BlockPos pos, BlockState state, IModelData tileData)
    {
        return tileData;
    }

    private static String generateKey(Map<String, String> textures)
    {
        if (textures == null)
        {
            return "";
        }
        else
        {
            return StringUtils.join(textures.entrySet(), ',');
        }
    }

    public static class TextureOverrideList extends ItemOverrideList
    {
        private final TextureModel baked;
        private final Cache<ItemStack, IBakedModel> cache = CacheBuilder.newBuilder().maximumSize(100L).expireAfterWrite(300L, TimeUnit.SECONDS).weakKeys().build();

        public TextureOverrideList(TextureModel model)
        {
            this.baked = model;
        }

        @Override
        public IBakedModel getModelWithOverrides(IBakedModel model, ItemStack stack, World worldIn, LivingEntity entityIn)
        {
            if (model instanceof TextureModel)
            {
                try
                {
                    model = cache.get(stack, () -> {
                        CompoundNBT data = NBTHelper.of(stack).getTag("BlockEntityTag.Textures");
                        if (data == null)
                        {
                            return baked.originalBaked;
                        }
                        Set<String> keySet = data.keySet();
                        Map<String, String> overrides = Maps.newHashMapWithExpectedSize(keySet.size());
                        keySet.forEach(k -> overrides.put(k, ""));
                        TextureTile.readTextures(overrides, data);
                        return baked.getModel(overrides);
                    });
                }
                catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
            }
            return model;
        }

        @Override
        public ImmutableList<ItemOverride> getOverrides()
        {
            return ImmutableList.of();
        }
    }

    @Override
    public boolean func_230044_c_() {
        return originalBaked.func_230044_c_();
    }
}

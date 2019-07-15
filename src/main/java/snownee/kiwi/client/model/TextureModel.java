package snownee.kiwi.client.model;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverride;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.model.Variant;
import net.minecraft.client.renderer.model.VariantList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
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
import snownee.kiwi.test.TestModule;
import snownee.kiwi.tile.TextureTile;
import snownee.kiwi.util.NBTHelper;

@EventBusSubscriber(bus = Bus.MOD, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class TextureModel implements IDynamicBakedModel
{
    public static ModelProperty<Map<String, String>> TEXTURES = new ModelProperty<>();

    public static void register(ModelBakeEvent event, Block block, @Nullable BlockState inventoryState)
    {
        block.getStateContainer().getValidStates().forEach(s -> {
            ModelResourceLocation rl = BlockModelShapes.getModelLocation(s);
            register(event, rl, s.equals(inventoryState));
        });
    }

    public static void register(ModelBakeEvent event, ModelResourceLocation rl, boolean inventory)
    {
        IUnbakedModel unbakedModel = ModelLoaderRegistry.getModelOrLogError(rl, "Kiwi failed to replace block model " + rl);
        IBakedModel bakedModel = event.getModelRegistry().get(rl);
        TextureModel textureModel = null;

        if (bakedModel != null)
        {
            if (unbakedModel instanceof VariantList)
            {
                VariantList variantList = (VariantList) unbakedModel;
                List<Variant> variants = variantList.getVariantList();
                for (Variant variant : variants)
                {
                    IUnbakedModel unbakedModel2 = event.getModelLoader().getUnbakedModel(variant.getModelLocation());
                    if (unbakedModel2 instanceof BlockModel)
                    {
                        TextureModel model = new TextureModel(event.getModelLoader(), (BlockModel) unbakedModel2, bakedModel, variant, inventory);
                        event.getModelRegistry().put(rl, model);
                        if (inventory)
                        {
                            textureModel = model;
                            inventory = false;
                        }
                    }
                }
            }
        }
        if (textureModel != null)
        {
            rl = new ModelResourceLocation(rl, "inventory");
            event.getModelRegistry().put(rl, textureModel);
            ItemModelMesher mesher = Minecraft.getInstance().getItemRenderer().getItemModelMesher();
            mesher.register(TestModule.FIRST_BLOCK.asItem(), rl);
        }
    }

    private final ModelLoader modelLoader;
    private final Variant variant;
    private final BlockModel originalUnbaked;
    private final IBakedModel originalBaked;
    private final TextureOverrideList overrideList;
    private final Cache<String, IBakedModel> baked = CacheBuilder.newBuilder().maximumSize(200L).expireAfterWrite(500L, TimeUnit.SECONDS).weakKeys().build();

    public TextureModel(ModelLoader modelLoader, BlockModel originalUnbaked, IBakedModel originalBaked, Variant variant, boolean inventory)
    {
        this.modelLoader = modelLoader;
        this.originalUnbaked = originalUnbaked;
        this.originalBaked = originalBaked;
        this.variant = variant;
        overrideList = inventory ? new TextureOverrideList(this) : null;
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
        return originalBaked.isBuiltInRenderer();
    }

    @Override
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
        Map<String, String> textures = Maps.newHashMap(originalUnbaked.textures);
        if (overrides == null)
        {
            return originalBaked;
        }
        overrides.forEach((k, v) -> {
            if (!v.isEmpty())
                textures.put(k, v);
        });
        BlockModel unbaked = new BlockModel(originalUnbaked.getParentLocation(), originalUnbaked.getElements(), textures, originalUnbaked.isAmbientOcclusion(), originalUnbaked.isGui3d(), originalUnbaked.getAllTransforms(), Lists.newArrayList(originalUnbaked.getOverrides()));
        String key = generateKey(overrides);
        IBakedModel model = null;
        try
        {
            model = baked.get(key, () -> unbaked.bake(modelLoader, ModelLoader.defaultTextureGetter(), variant, DefaultVertexFormats.BLOCK));
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
            return originalBaked;
        }
        return model;
    }

    @Override
    public IModelData getModelData(IEnviromentBlockReader world, BlockPos pos, BlockState state, IModelData tileData)
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
            String str = textures.toString();
            return str.substring(1, str.length() - 1);
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
}

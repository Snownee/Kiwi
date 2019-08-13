package snownee.kiwi.client.model;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

/**
 * 
 * @since 2.3.0
 * @author Snownee
 *
 */
@OnlyIn(Dist.CLIENT)
public class TextureMultipart implements IDynamicBakedModel
{
    private final List<Pair<Predicate<BlockState>, IBakedModel>> selectors;
    private final IBakedModel originalBaked;
    private final Map<BlockState, BitSet> field_210277_g = new Object2ObjectOpenCustomHashMap<>(Util.identityHashStrategy());

    public TextureMultipart(List<Pair<Predicate<BlockState>, IBakedModel>> p_i48273_1_)
    {
        this.selectors = p_i48273_1_;
        originalBaked = p_i48273_1_.iterator().next().getRight();
    }

    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData extraData)
    {
        if (state == null)
        {
            return Collections.emptyList();
        }
        else
        {
            BitSet bitset = this.field_210277_g.get(state);
            if (bitset == null)
            {
                bitset = new BitSet();

                for (int i = 0; i < this.selectors.size(); ++i)
                {
                    Pair<Predicate<BlockState>, IBakedModel> pair = this.selectors.get(i);
                    if (pair.getLeft().test(state))
                    {
                        bitset.set(i);
                    }
                }

                this.field_210277_g.put(state, bitset);
            }

            List<BakedQuad> list = Lists.newArrayList();
            long k = rand.nextLong();

            for (int j = 0; j < bitset.length(); ++j)
            {
                if (bitset.get(j))
                {
                    list.addAll(this.selectors.get(j).getRight().getQuads(state, side, new Random(k), extraData));
                }
            }

            return list;
        }
    }

    public boolean isAmbientOcclusion()
    {
        return originalBaked.isAmbientOcclusion();
    }

    public boolean isGui3d()
    {
        return originalBaked.isGui3d();
    }

    public boolean isBuiltInRenderer()
    {
        return false;
    }

    @SuppressWarnings("deprecation")
    public TextureAtlasSprite getParticleTexture()
    {
        return originalBaked.getParticleTexture();
    }

    @Override
    public TextureAtlasSprite getParticleTexture(IModelData data)
    {
        return originalBaked.getParticleTexture(data);
    }

    @SuppressWarnings("deprecation")
    public ItemCameraTransforms getItemCameraTransforms()
    {
        return originalBaked.getItemCameraTransforms();
    }

    public ItemOverrideList getOverrides()
    {
        return originalBaked.getOverrides();
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder
    {
        private final List<Pair<Predicate<BlockState>, IBakedModel>> selectors = Lists.newArrayList();

        public void putModel(Predicate<BlockState> predicate, IBakedModel model)
        {
            this.selectors.add(Pair.of(predicate, model));
        }

        public IBakedModel build()
        {
            return new TextureMultipart(this.selectors);
        }
    }
}

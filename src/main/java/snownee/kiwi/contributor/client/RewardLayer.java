package snownee.kiwi.contributor.client;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.Contributors;

@OnlyIn(Dist.CLIENT)
public class RewardLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

    public static final Collection<RewardLayer> ALL_LAYERS = Lists.newLinkedList();
    private final Cache<ResourceLocation, LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> rewardId2renderer;
    private final Cache<String, LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> player2renderer;

    public RewardLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRendererIn) {
        super(entityRendererIn);
        if (getClass() == RewardLayer.class) {
            rewardId2renderer = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
            player2renderer = CacheBuilder.newBuilder().build();
        } else {
            rewardId2renderer = null;
            player2renderer = null;
        }
    }

    @Override
    public void func_225628_a_(MatrixStack matrix, IRenderTypeBuffer buffer, int p_225628_3_, AbstractClientPlayerEntity entityIn, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        if (player2renderer == null || rewardId2renderer == null) {
            return;
        }
        try {
            LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> renderer = player2renderer.getIfPresent(entityIn.getGameProfile().getName());
            if (renderer == null) {
                ResourceLocation id = Contributors.PLAYER_EFFECTS.get(entityIn.getGameProfile().getName());
                if (id != null) {
                    renderer = rewardId2renderer.get(id, () -> {
                        return Contributors.REWARD_PROVIDERS.get(id.getNamespace().toLowerCase(Locale.ENGLISH)).createRenderer(entityRenderer, id.getPath());
                    });
                    player2renderer.put(entityIn.getGameProfile().getName(), renderer);
                }
            }
            if (renderer != null) {
                renderer.func_225628_a_(matrix, buffer, p_225628_3_, entityIn, p_225628_5_, p_225628_6_, p_225628_7_, p_225628_8_, p_225628_9_, p_225628_10_);
            }
        } catch (ExecutionException e) {
            Kiwi.logger.catching(e);
        }
    }

    public Cache<String, LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> getCache() {
        return player2renderer;
    }

}

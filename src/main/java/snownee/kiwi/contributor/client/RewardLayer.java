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
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (player2renderer == null || rewardId2renderer == null) {
            return;
        }
        try {
            LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> renderer = player2renderer.getIfPresent(entitylivingbaseIn.getGameProfile().getName());
            if (renderer == null) {
                ResourceLocation id = Contributors.PLAYER_EFFECTS.get(entitylivingbaseIn.getGameProfile().getName());
                if (id != null) {
                    renderer = rewardId2renderer.get(id, () -> {
                        return Contributors.REWARD_PROVIDERS.get(id.getNamespace().toLowerCase(Locale.ENGLISH)).createRenderer(entityRenderer, id.getPath());
                    });
                    player2renderer.put(entitylivingbaseIn.getGameProfile().getName(), renderer);
                }
            }
            if (renderer != null) {
                renderer.render(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        } catch (ExecutionException e) {
            Kiwi.logger.catching(e);
        }
    }

    public Cache<String, LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> getCache() {
        return player2renderer;
    }

}

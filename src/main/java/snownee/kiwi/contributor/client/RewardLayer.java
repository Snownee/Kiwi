package snownee.kiwi.contributor.client;

import java.util.Collection;
import java.util.Locale;

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
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.contributor.ITierProvider;

@OnlyIn(Dist.CLIENT)
public class RewardLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

	public static final Collection<RewardLayer> ALL_LAYERS = Lists.newLinkedList();
	private final Cache<String, LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> player2renderer;

	public RewardLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> entityRendererIn) {
		super(entityRendererIn);
		if (getClass() == RewardLayer.class) {
			player2renderer = CacheBuilder.newBuilder().build();
		} else {
			player2renderer = null;
		}
	}

	@Override
	public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, AbstractClientPlayerEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (player2renderer == null) {
			return;
		}
		LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> renderer = player2renderer.getIfPresent(entitylivingbaseIn.getGameProfile().getName());
		if (renderer == null) {
			String name = entitylivingbaseIn.getGameProfile().getName();
			ResourceLocation id = Contributors.PLAYER_EFFECTS.get(name);
			if (id != null) {
				ITierProvider provider = Contributors.REWARD_PROVIDERS.get(id.getNamespace().toLowerCase(Locale.ENGLISH));
				if (provider == null) {
					Contributors.PLAYER_EFFECTS.remove(name);
				} else {
					renderer = provider.createRenderer(entityRenderer, id.getPath());
					player2renderer.put(name, renderer);
				}
			}
		}
		if (renderer != null) {
			renderer.render(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
		}
	}

	public Cache<String, LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> getCache() {
		return player2renderer;
	}

}

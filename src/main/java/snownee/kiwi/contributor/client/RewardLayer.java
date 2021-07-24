package snownee.kiwi.contributor.client;

import java.util.Collection;
import java.util.Locale;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.contributor.ITierProvider;

@OnlyIn(Dist.CLIENT)
public class RewardLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

	public static final Collection<RewardLayer> ALL_LAYERS = Lists.newLinkedList();
	private final Cache<String, RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> player2renderer;

	public RewardLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> entityRendererIn) {
		super(entityRendererIn);
		if (getClass() == RewardLayer.class) {
			player2renderer = CacheBuilder.newBuilder().build();
		} else {
			player2renderer = null;
		}
	}

	@Override
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (player2renderer == null) {
			return;
		}
		RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer = player2renderer.getIfPresent(entitylivingbaseIn.getGameProfile().getName());
		if (renderer == null) {
			String name = entitylivingbaseIn.getGameProfile().getName();
			ResourceLocation id = Contributors.PLAYER_EFFECTS.get(name);
			if (id != null) {
				ITierProvider provider = Contributors.REWARD_PROVIDERS.get(id.getNamespace().toLowerCase(Locale.ENGLISH));
				if (provider == null) {
					Contributors.PLAYER_EFFECTS.remove(name);
				} else {
					renderer = provider.createRenderer(this.renderer, id.getPath());
					if (renderer != null)
						player2renderer.put(name, renderer);
				}
			}
		}
		if (renderer != null) {
			renderer.render(matrixStackIn, bufferIn, packedLightIn, entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
		}
	}

	public Cache<String, RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> getCache() {
		return player2renderer;
	}

}

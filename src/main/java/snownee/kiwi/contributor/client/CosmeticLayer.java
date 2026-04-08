package snownee.kiwi.contributor.client;

import java.util.Collection;
import java.util.Locale;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.contributor.ITierProvider;

public class CosmeticLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

	public static final Collection<CosmeticLayer> ALL_LAYERS = Lists.newLinkedList();
	private final Cache<String, RenderLayer<AvatarRenderState, PlayerModel>> player2renderer;
	public final RenderLayerParent<AvatarRenderState, PlayerModel> renderer;

	public CosmeticLayer(RenderLayerParent<AvatarRenderState, PlayerModel> entityRendererIn) {
		super(entityRendererIn);
		this.renderer = entityRendererIn;
		if (getClass() == CosmeticLayer.class) {
			player2renderer = CacheBuilder.newBuilder().build();
		} else {
			player2renderer = null;
		}
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
		if (player2renderer == null) {
			return;
		}
		String name = ((CosmeticRenderState) state).kiwi$getName();
		if (name == null) {
			return;
		}
		RenderLayer<AvatarRenderState, PlayerModel> renderer = player2renderer.getIfPresent(name);
		if (renderer == null) {
			Identifier id = Contributors.PLAYER_COSMETICS.get(name);
			if (id != null) {
				ITierProvider provider = Contributors.REWARD_PROVIDERS.get(id.getNamespace().toLowerCase(Locale.ENGLISH));
				if (provider == null) {
					Contributors.PLAYER_COSMETICS.remove(name);
				} else {
					renderer = provider.createRenderer(this.renderer, id.getPath());
					if (renderer != null) {
						player2renderer.put(name, renderer);
					}
				}
			}
		}
		if (renderer != null) {
			renderer.submit(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);
		}
	}

	public Cache<String, RenderLayer<AvatarRenderState, PlayerModel>> getCache() {
		return player2renderer;
	}

}


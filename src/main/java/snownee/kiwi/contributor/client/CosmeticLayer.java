package snownee.kiwi.contributor.client;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.contributor.CosmeticRenderState;
import snownee.kiwi.mixin.client.RenderLayerAccess;

public class CosmeticLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

	public static Map<PlayerModelType, CosmeticLayer> ALL_LAYERS = Maps.newHashMap();
	private static final Map<UUID, CosmeticLayer> PLAYER_CACHE = Maps.newHashMap();
	private static final Map<Identifier, Function<RenderLayerParent<AvatarRenderState, PlayerModel>, CosmeticLayer>> LAYER_CREATORS = Maps.newHashMap();
	private final Map<Identifier, CosmeticLayer> renderers = Maps.newHashMap();

	public CosmeticLayer(RenderLayerParent<AvatarRenderState, PlayerModel> entityRendererIn) {
		super(entityRendererIn);
	}

	@Nullable
	public static CosmeticLayer getRendererOf(AbstractClientPlayer player) {
		return PLAYER_CACHE.computeIfAbsent(
				player.getUUID(), uuid -> {
					CosmeticLayer parent = ALL_LAYERS.get(player.getSkin().model());
					Identifier id = Contributors.PLAYER_COSMETICS.get(uuid);
					if (parent == null || id == null) {
						return null;
					}
					return createRenderer(id, parent);
				});
	}

	@Nullable
	public static CosmeticLayer createRenderer(Identifier id, CosmeticLayer parent) {
		return parent.renderers.computeIfAbsent(
				id, key -> {
					Function<RenderLayerParent<AvatarRenderState, PlayerModel>, CosmeticLayer> creator = LAYER_CREATORS.get(key);
					if (creator != null) {
						//noinspection unchecked,rawtypes
						RenderLayerParent<AvatarRenderState, PlayerModel> layerParent = ((RenderLayerAccess) parent).getRenderer();
						return creator.apply(layerParent);
					}
					return null;
				});
	}

	public synchronized static void registerRenderer(
			Identifier id,
			Function<RenderLayerParent<AvatarRenderState, PlayerModel>, CosmeticLayer> creator) {
		LAYER_CREATORS.put(id, creator);
	}

	@Override
	public void submit(
			PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector,
			int lightCoords,
			AvatarRenderState renderState,
			float yRot,
			float xRot) {
		if (renderState.isInvisible) {
			return;
		}
		CosmeticLayer renderer = ((CosmeticRenderState) renderState).kiwi$getCosmeticLayer();
		if (renderer != null) {
			renderer.submit(poseStack, submitNodeCollector, lightCoords, renderState, yRot, xRot);
		}
	}

	public static Map<UUID, CosmeticLayer> getCache() {
		return PLAYER_CACHE;
	}
}
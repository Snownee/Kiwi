package snownee.kiwi.contributor.client;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.mixin.client.RenderLayerAccess;

public class CosmeticLayer extends RenderLayer<PlayerRenderState, PlayerModel> {

	public static final ContextKey<CosmeticLayer> COSMETIC_KEY = new ContextKey<>(Kiwi.id("cosmetic"));
	public static ImmutableMap<PlayerSkin.Model, CosmeticLayer> ALL_LAYERS = ImmutableMap.of();
	private static final Map<UUID, CosmeticLayer> PLAYER_CACHE = Maps.newHashMap();
	private static final Map<ResourceLocation, Function<RenderLayerParent<PlayerRenderState, PlayerModel>, CosmeticLayer>> LAYER_CREATORS = Maps.newHashMap();
	private final Map<ResourceLocation, CosmeticLayer> renderers = Maps.newHashMap();

	public CosmeticLayer(RenderLayerParent<PlayerRenderState, PlayerModel> entityRendererIn) {
		super(entityRendererIn);
	}

	@Nullable
	public static CosmeticLayer getRendererOf(AbstractClientPlayer player) {
		return PLAYER_CACHE.computeIfAbsent(
				player.getUUID(), uuid -> {
					CosmeticLayer parent = ALL_LAYERS.get(player.getSkin().model());
					ResourceLocation id = Contributors.PLAYER_COSMETICS.get(uuid);
					if (parent == null || id == null) {
						return null;
					}
					return createRenderer(id, parent);
				});
	}

	@Nullable
	public static CosmeticLayer createRenderer(ResourceLocation id, CosmeticLayer parent) {
		return parent.renderers.computeIfAbsent(
				id, key -> {
					Function<RenderLayerParent<PlayerRenderState, PlayerModel>, CosmeticLayer> creator = LAYER_CREATORS.get(key);
					if (creator != null) {
						//noinspection unchecked
						RenderLayerParent<PlayerRenderState, PlayerModel> layerParent = ((RenderLayerAccess<PlayerRenderState, PlayerModel>) parent).getRenderer();
						return creator.apply(layerParent);
					}
					return null;
				});
	}

	public synchronized static void registerRenderer(
			ResourceLocation id,
			Function<RenderLayerParent<PlayerRenderState, PlayerModel>, CosmeticLayer> creator) {
		LAYER_CREATORS.put(id, creator);
	}

	@Override
	public void render(
			PoseStack matrixStackIn,
			MultiBufferSource bufferIn,
			int packedLightIn,
			PlayerRenderState renderState,
			float yRot,
			float xRot) {
		if (renderState.isInvisible) {
			return;
		}
		CosmeticLayer renderer = renderState.getRenderData(COSMETIC_KEY);
		if (renderer != null) {
			renderer.render(matrixStackIn, bufferIn, packedLightIn, renderState, yRot, xRot);
		}
	}

	public static Map<UUID, CosmeticLayer> getCache() {
		return PLAYER_CACHE;
	}
}

package snownee.kiwi.contributor.impl.client.layer;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.impl.client.model.PlanetModel;

public class PlanetLayer extends CosmeticLayer {
	private static final Identifier TEXTURE = Kiwi.id("textures/reward/planet.png");
	private static final Supplier<LayerDefinition> definition = Suppliers.memoize(PlanetModel::create);
	private final PlanetModel<AvatarRenderState> modelPlanet;

	public PlanetLayer(RenderLayerParent<AvatarRenderState, PlayerModel> entityRendererIn) {
		super(entityRendererIn);
		modelPlanet = new PlanetModel<>(definition.get().bakeRoot());
	}

	@Override
	public void submit(
			PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector,
			int lightCoords,
			AvatarRenderState renderState,
			float yRot,
			float xRot) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(-renderState.ageInTicks));
		poseStack.scale(0.7f, 0.7f, 0.7f);
		submitNodeCollector.submitModel(
				modelPlanet,
				renderState,
				poseStack,
				RenderTypes.entityTranslucent(TEXTURE),
				lightCoords,
				OverlayTexture.NO_OVERLAY,
				renderState.outlineColor,
				null
		);
		poseStack.popPose();
	}
}

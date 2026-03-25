package snownee.kiwi.contributor.impl.client.layer;

import java.util.Locale;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import snownee.kiwi.contributor.CosmeticRenderState;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.impl.client.model.FoxTailModel;

public class FoxTailLayer extends CosmeticLayer {
	private static final Identifier FOX = Identifier.withDefaultNamespace("textures/entity/fox/fox.png");
	private static final Identifier SNOW_FOX = Identifier.withDefaultNamespace("textures/entity/fox/snow_fox.png");
	private static final Supplier<LayerDefinition> definition = Suppliers.memoize(FoxTailModel::create);
	private final FoxTailModel<AvatarRenderState> modelFoxTail;

	public FoxTailLayer(RenderLayerParent<AvatarRenderState, PlayerModel> entityRendererIn) {
		super(entityRendererIn);
		modelFoxTail = new FoxTailModel<>(entityRendererIn.getModel(), definition.get().bakeRoot());
	}

	@Override
	public void submit(
			PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector,
			int lightCoords,
			AvatarRenderState renderState,
			float yRot,
			float xRot) {
		if (renderState.showCape && renderState.chestEquipment.get(DataComponents.GLIDER) != null) {
			return;
		}

		String name = ((CosmeticRenderState) renderState).kiwi$getName().toLowerCase(Locale.ENGLISH);
		Identifier texture = name.contains("snow") || name.contains("xue") || name.contains("yuki") ? SNOW_FOX : FOX;

		poseStack.pushPose();
		submitNodeCollector.submitModel(
				modelFoxTail,
				renderState,
				poseStack,
				RenderTypes.entitySolid(texture),
				lightCoords,
				OverlayTexture.NO_OVERLAY,
				renderState.outlineColor,
				null
		);
		poseStack.popPose();
	}
}

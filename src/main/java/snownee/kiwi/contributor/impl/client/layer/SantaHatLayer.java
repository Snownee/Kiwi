package snownee.kiwi.contributor.impl.client.layer;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.impl.client.model.SantaHatModel;

public class SantaHatLayer extends CosmeticLayer {
	private static final Identifier TEXTURE = Kiwi.id("textures/reward/santa.png");
	private static final Supplier<LayerDefinition> definition = Suppliers.memoize(SantaHatModel::create);
	private final SantaHatModel modelSantaHat;

	public SantaHatLayer(RenderLayerParent<AvatarRenderState, PlayerModel> entityRendererIn) {
		super(entityRendererIn);
		modelSantaHat = new SantaHatModel(entityRendererIn.getModel(), definition.get());
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
		if (state.isInvisible) {
			return;
		}
		if (!state.headEquipment.isEmpty()) {
			return;
		}
		poseStack.pushPose();
		modelSantaHat.setupAnim(state);
		submitNodeCollector.submitModel(modelSantaHat, state, poseStack, RenderTypes.entitySolid(TEXTURE), lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
		poseStack.popPose();
	}

}

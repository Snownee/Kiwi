package snownee.kiwi.contributor.impl.client.layer;

import java.util.Locale;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ElytraItem;
import snownee.kiwi.contributor.client.CosmeticLayer;
import snownee.kiwi.contributor.client.CosmeticRenderState;
import snownee.kiwi.contributor.impl.client.model.FoxTailModel;

public class FoxTailLayer extends CosmeticLayer {
	private static final Identifier FOX = Identifier.withDefaultNamespace("textures/entity/fox/fox.png");
	private static final Identifier SNOW_FOX = Identifier.withDefaultNamespace("textures/entity/fox/snow_fox.png");
	private static final Supplier<LayerDefinition> definition = Suppliers.memoize(FoxTailModel::create);
	private final FoxTailModel modelFoxTail;

	public FoxTailLayer(RenderLayerParent<AvatarRenderState, PlayerModel> entityRendererIn) {
		super(entityRendererIn);
		modelFoxTail = new FoxTailModel(entityRendererIn.getModel(), definition.get());
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
		if (state.isInvisible || state.hasPose(Pose.SLEEPING)) {
			return;
		}
		if (state.chestEquipment.getItem() instanceof ElytraItem) {
			return;
		}
		String name = ((CosmeticRenderState) state).kiwi$getName();
		String nameLower = name != null ? name.toLowerCase(Locale.ENGLISH) : "";
		Identifier texture = nameLower.contains("snow") || nameLower.contains("xue") || nameLower.contains("yuki") ? SNOW_FOX : FOX;
		poseStack.pushPose();
		modelFoxTail.setupAnim(state);
		submitNodeCollector.submitModel(modelFoxTail, state, poseStack, RenderTypes.entitySolid(texture), lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
		poseStack.popPose();
	}

}


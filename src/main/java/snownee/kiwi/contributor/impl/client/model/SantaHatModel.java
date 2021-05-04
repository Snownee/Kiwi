package snownee.kiwi.contributor.impl.client.model;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SantaHatModel<T extends LivingEntity> extends AgeableModel<T> {

	private PlayerModel<AbstractClientPlayerEntity> playerModel;
	private ModelRenderer bb_main;

	public SantaHatModel(PlayerModel<AbstractClientPlayerEntity> playerModel) {
		this.playerModel = playerModel;
		textureWidth = 32;
		textureHeight = 32;

		bb_main = new ModelRenderer(this);
		bb_main.setTextureOffset(0, 8).addBox(-0.5F, -10.0F, -3.0F, 5.0F, 2.0F, 5.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 0).addBox(0.0F, -12.0F, -2.5F, 4.0F, 3.0F, 4.0F, 0.0F, false);
		bb_main.setTextureOffset(16, 4).addBox(4.0F, -12.5F, -1.5F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		bb_main.setTextureOffset(12, 0).addBox(5.0F, -9.0F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);
	}

	@Override
	protected Iterable<ModelRenderer> getHeadParts() {
		return ImmutableList.of(bb_main);
	}

	@Override
	protected Iterable<ModelRenderer> getBodyParts() {
		return ImmutableList.of();
	}

	@Override
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		bb_main.copyModelAngles(playerModel.bipedHead);
	}

}

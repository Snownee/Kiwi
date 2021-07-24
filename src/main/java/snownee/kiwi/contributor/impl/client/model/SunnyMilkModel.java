package snownee.kiwi.contributor.impl.client.model;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SunnyMilkModel<T extends LivingEntity> extends AgeableModel<T> {

	private float ticks;
	private ModelRenderer wingRight;
	private ModelRenderer wingLeft;

	public SunnyMilkModel(PlayerModel<AbstractClientPlayerEntity> playerModel) {
		texWidth = 64;
		texHeight = 64;

		wingRight = new ModelRenderer(this);
		wingRight.setPos(1.9641F, -2.0F, 2.0F);
		wingRight.texOffs(0, 12).addBox(-0.5F, -5.5F, 0.0F, 0.0F, 12.0F, 20.0F, 0.0F, false);
		wingRight.texOffs(0, 28).addBox(-0.5F, 6.5F, 0.0F, 0.0F, 10.0F, 16.0F, 0.0F, false);

		wingLeft = new ModelRenderer(this);
		wingLeft.setPos(-1.9641F, -2.0F, 2.0F);
		wingLeft.texOffs(0, 12).addBox(0.5F, -5.5F, 0.0F, 0.0F, 12.0F, 20.0F, 0.0F, false);
		wingLeft.texOffs(0, 28).addBox(0.5F, 6.5F, 0.0F, 0.0F, 10.0F, 16.0F, 0.0F, false);
	}

	@Override
	protected Iterable<ModelRenderer> headParts() {
		return ImmutableList.of();
	}

	@Override
	protected Iterable<ModelRenderer> bodyParts() {
		return ImmutableList.of(wingLeft, wingRight);
	}

	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float netHeadYaw, float headPitch) {
		float f = ((float) entityIn.getDeltaMovement().length()) * 10;
		ticks += Minecraft.getInstance().getDeltaFrameTime() * (1 + Math.min(9, f * f * f)) * 0.1f;
		wingLeft.yRot = -1.0472F + MathHelper.sin(ticks) * 0.25f;
		wingRight.yRot = -wingLeft.yRot;
	}

}

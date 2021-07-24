package snownee.kiwi.contributor.impl.client.model;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoxTailModel<T extends LivingEntity> extends AgeableModel<T> {

	private PlayerModel<AbstractClientPlayerEntity> playerModel;
	private ModelRenderer tail;
	private ModelRenderer ear1;
	private ModelRenderer ear2;

	public FoxTailModel(PlayerModel<AbstractClientPlayerEntity> playerModel) {
		this.playerModel = playerModel;
		texWidth = 48;
		texHeight = 32;

		this.ear1 = new ModelRenderer(this, 8, 1);
		this.ear1.addBox(-4.0F, -10.0F, -4.0F, 2.0F, 2.0F, 1.0F);
		this.ear2 = new ModelRenderer(this, 15, 1);
		this.ear2.addBox(2.0F, -10.0F, -4.0F, 2.0F, 2.0F, 1.0F);
		this.tail = new ModelRenderer(this, 30, 0);
		this.tail.addBox(0F, 0F, 0F, 4.0F, 9.0F, 5.0F);
	}

	@Override
	protected Iterable<ModelRenderer> headParts() {
		return ImmutableList.of(ear1, ear2);
	}

	@Override
	protected Iterable<ModelRenderer> bodyParts() {
		return ImmutableList.of(tail);
	}

	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		ear1.copyFrom(playerModel.head);
		ear2.copyFrom(playerModel.head);
		if (ageInTicks % 60 < 2) {
			ear1.yRot += 0.05f;
			ear2.yRot -= 0.05f;
		}
		float delta = MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
		if (entityIn.isCrouching()) {
			this.tail.setPos(-2.0F, 14.0F, 5.5F);
			this.tail.xRot = 1.25F + delta;
		} else {
			this.tail.setPos(-2.0F, 10.0F, .5F);
			this.tail.xRot = 0.85F + delta;
		}

	}

}

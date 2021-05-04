package snownee.kiwi.contributor.impl.client.model;

import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ElectronicatModel<T extends LivingEntity> extends AgeableModel<T> {

	private static final Random RANDOM = new Random();
	private float ticks;
	private ModelRenderer earphone;

	public ElectronicatModel(PlayerModel<AbstractClientPlayerEntity> playerModel) {
		textureWidth = 32;
		textureHeight = 32;

		earphone = new ModelRenderer(this);
		earphone.setTextureOffset(0, 0).addBox(-5.5F, -6.25F, -2.5F, 1.0F, 4.0F, 4.0F, 0.0F, true);
		earphone.setTextureOffset(0, 0).addBox(-4.5F, -7.25F, -1.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		earphone.setTextureOffset(0, 0).addBox(3.5F, -7.25F, -1.0F, 1.0F, 1.0F, 1.0F, 0.0F, true);
		earphone.setTextureOffset(6, 0).addBox(-4.5F, -8.25F, -1.0F, 9.0F, 1.0F, 1.0F, 0.0F, false);
		earphone.setTextureOffset(6, 2).addBox(1.5F, -9.25F, -1.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);
		earphone.setTextureOffset(6, 2).addBox(-3.5F, -9.25F, -1.0F, 2.0F, 1.0F, 1.0F, 0.0F, true);
		earphone.setTextureOffset(0, 0).addBox(4.5F, -6.25F, -2.5F, 1.0F, 4.0F, 4.0F, 0.0F, false);
	}

	@Override
	protected Iterable<ModelRenderer> getHeadParts() {
		return ImmutableList.of(earphone);
	}

	@Override
	protected Iterable<ModelRenderer> getBodyParts() {
		return ImmutableList.of();
	}

	@Override
	public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float netHeadYaw, float headPitch) {
	}

	public static class Emissive<T extends LivingEntity> extends AgeableModel<T> {

		private ModelRenderer earphone;

		public Emissive(PlayerModel<?> playerModel) {
			textureWidth = 32;
			textureHeight = 32;

			earphone = new ModelRenderer(this);
			earphone.setTextureOffset(22, 0).addBox(3.5F, -6.25F, -2.5F, 1.0F, 4.0F, 4.0F, 0.0F, false);
			earphone.setTextureOffset(22, 0).addBox(-4.5F, -6.25F, -2.5F, 1.0F, 4.0F, 4.0F, 0.0F, true);
			earphone.setTextureOffset(12, 2).addBox(1.5F, -9.25F, -1.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);
			earphone.setTextureOffset(12, 2).addBox(-3.5F, -9.25F, -1.0F, 2.0F, 1.0F, 1.0F, 0.0F, true);
		}

		@Override
		protected Iterable<ModelRenderer> getHeadParts() {
			return ImmutableList.of(earphone);
		}

		@Override
		protected Iterable<ModelRenderer> getBodyParts() {
			return ImmutableList.of();
		}

		@Override
		public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
			//            Minecraft.getInstance().particles.emitParticleAtEntity(entityIn, dataIn, lifetimeIn);
		}

	}
}

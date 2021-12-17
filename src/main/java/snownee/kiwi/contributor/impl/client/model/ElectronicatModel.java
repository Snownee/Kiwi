//package snownee.kiwi.contributor.impl.client.model;
//
//import java.util.Random;
//
//import com.google.common.collect.ImmutableList;
//
//import net.minecraft.client.model.AgeableListModel;
//import net.minecraft.client.model.PlayerModel;
//import net.minecraft.client.model.geom.ModelPart;
//import net.minecraft.client.player.AbstractClientPlayer;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//
//@Environment(EnvType.CLIENT)
//public class ElectronicatModel<T extends LivingEntity> extends AgeableListModel<T> {
//
//	private static final Random RANDOM = new Random();
//	private float ticks;
//	private ModelPart earphone;
//
//	public ElectronicatModel(PlayerModel<AbstractClientPlayer> playerModel) {
//		texWidth = 32;
//		texHeight = 32;
//
//		earphone = new ModelPart(this);
//		earphone.texOffs(0, 0).addBox(-5.5F, -6.25F, -2.5F, 1.0F, 4.0F, 4.0F, 0.0F, true);
//		earphone.texOffs(0, 0).addBox(-4.5F, -7.25F, -1.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
//		earphone.texOffs(0, 0).addBox(3.5F, -7.25F, -1.0F, 1.0F, 1.0F, 1.0F, 0.0F, true);
//		earphone.texOffs(6, 0).addBox(-4.5F, -8.25F, -1.0F, 9.0F, 1.0F, 1.0F, 0.0F, false);
//		earphone.texOffs(6, 2).addBox(1.5F, -9.25F, -1.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);
//		earphone.texOffs(6, 2).addBox(-3.5F, -9.25F, -1.0F, 2.0F, 1.0F, 1.0F, 0.0F, true);
//		earphone.texOffs(0, 0).addBox(4.5F, -6.25F, -2.5F, 1.0F, 4.0F, 4.0F, 0.0F, false);
//	}
//
//	@Override
//	protected Iterable<ModelPart> headParts() {
//		return ImmutableList.of(earphone);
//	}
//
//	@Override
//	protected Iterable<ModelPart> bodyParts() {
//		return ImmutableList.of();
//	}
//
//	@Override
//	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float netHeadYaw, float headPitch) {
//	}
//
//	public static class Emissive<T extends LivingEntity> extends AgeableListModel<T> {
//
//		private ModelPart earphone;
//
//		public Emissive(PlayerModel<?> playerModel) {
//			texWidth = 32;
//			texHeight = 32;
//
//			earphone = new ModelPart(this);
//			earphone.texOffs(22, 0).addBox(3.5F, -6.25F, -2.5F, 1.0F, 4.0F, 4.0F, 0.0F, false);
//			earphone.texOffs(22, 0).addBox(-4.5F, -6.25F, -2.5F, 1.0F, 4.0F, 4.0F, 0.0F, true);
//			earphone.texOffs(12, 2).addBox(1.5F, -9.25F, -1.0F, 2.0F, 1.0F, 1.0F, 0.0F, false);
//			earphone.texOffs(12, 2).addBox(-3.5F, -9.25F, -1.0F, 2.0F, 1.0F, 1.0F, 0.0F, true);
//		}
//
//		@Override
//		protected Iterable<ModelPart> headParts() {
//			return ImmutableList.of(earphone);
//		}
//
//		@Override
//		protected Iterable<ModelPart> bodyParts() {
//			return ImmutableList.of();
//		}
//
//		@Override
//		public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
//			//            Minecraft.getInstance().particles.emitParticleAtEntity(entityIn, dataIn, lifetimeIn);
//		}
//
//	}
//}

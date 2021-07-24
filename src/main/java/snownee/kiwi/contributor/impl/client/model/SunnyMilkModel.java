//package snownee.kiwi.contributor.impl.client.model;
//
//import com.google.common.collect.ImmutableList;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.model.AgeableListModel;
//import net.minecraft.client.model.PlayerModel;
//import net.minecraft.client.model.geom.ModelPart;
//import net.minecraft.client.player.AbstractClientPlayer;
//import net.minecraft.util.Mth;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//
//@OnlyIn(Dist.CLIENT)
//public class SunnyMilkModel<T extends LivingEntity> extends AgeableListModel<T> {
//
//	private float ticks;
//	private ModelPart wingRight;
//	private ModelPart wingLeft;
//
//	public SunnyMilkModel(PlayerModel<AbstractClientPlayer> playerModel) {
//		texWidth = 64;
//		texHeight = 64;
//
//		wingRight = new ModelPart(this);
//		wingRight.setPos(1.9641F, -2.0F, 2.0F);
//		wingRight.texOffs(0, 12).addBox(-0.5F, -5.5F, 0.0F, 0.0F, 12.0F, 20.0F, 0.0F, false);
//		wingRight.texOffs(0, 28).addBox(-0.5F, 6.5F, 0.0F, 0.0F, 10.0F, 16.0F, 0.0F, false);
//
//		wingLeft = new ModelPart(this);
//		wingLeft.setPos(-1.9641F, -2.0F, 2.0F);
//		wingLeft.texOffs(0, 12).addBox(0.5F, -5.5F, 0.0F, 0.0F, 12.0F, 20.0F, 0.0F, false);
//		wingLeft.texOffs(0, 28).addBox(0.5F, 6.5F, 0.0F, 0.0F, 10.0F, 16.0F, 0.0F, false);
//	}
//
//	@Override
//	protected Iterable<ModelPart> headParts() {
//		return ImmutableList.of();
//	}
//
//	@Override
//	protected Iterable<ModelPart> bodyParts() {
//		return ImmutableList.of(wingLeft, wingRight);
//	}
//
//	@Override
//	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float netHeadYaw, float headPitch) {
//		float f = ((float) entityIn.getDeltaMovement().length()) * 10;
//		ticks += Minecraft.getInstance().getDeltaFrameTime() * (1 + Math.min(9, f * f * f)) * 0.1f;
//		wingLeft.yRot = -1.0472F + Mth.sin(ticks) * 0.25f;
//		wingRight.yRot = -wingLeft.yRot;
//	}
//
//}

//package snownee.kiwi.contributor.impl.client.model;
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
//@OnlyIn(Dist.CLIENT)
//public class SantaHatModel<T extends LivingEntity> extends AgeableListModel<T> {
//
//	private PlayerModel<AbstractClientPlayer> playerModel;
//	private ModelPart bb_main;
//
//	public SantaHatModel(PlayerModel<AbstractClientPlayer> playerModel) {
//		this.playerModel = playerModel;
//		texWidth = 32;
//		texHeight = 32;
//
//		bb_main = new ModelPart(this);
//		bb_main.texOffs(0, 8).addBox(-0.5F, -10.0F, -3.0F, 5.0F, 2.0F, 5.0F, 0.0F, false);
//		bb_main.texOffs(0, 0).addBox(0.0F, -12.0F, -2.5F, 4.0F, 3.0F, 4.0F, 0.0F, false);
//		bb_main.texOffs(16, 4).addBox(4.0F, -12.5F, -1.5F, 2.0F, 4.0F, 2.0F, 0.0F, false);
//		bb_main.texOffs(12, 0).addBox(5.0F, -9.0F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);
//	}
//
//	@Override
//	protected Iterable<ModelPart> headParts() {
//		return ImmutableList.of(bb_main);
//	}
//
//	@Override
//	protected Iterable<ModelPart> bodyParts() {
//		return ImmutableList.of();
//	}
//
//	@Override
//	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
//		bb_main.copyFrom(playerModel.head);
//	}
//
//}

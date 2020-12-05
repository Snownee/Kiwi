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
        textureWidth = 64;
        textureHeight = 64;

        wingRight = new ModelRenderer(this);
        wingRight.setRotationPoint(1.9641F, -2.0F, 2.0F);
        wingRight.setTextureOffset(0, 12).addBox(-0.5F, -5.5F, 0.0F, 0.0F, 12.0F, 20.0F, 0.0F, false);
        wingRight.setTextureOffset(0, 28).addBox(-0.5F, 6.5F, 0.0F, 0.0F, 10.0F, 16.0F, 0.0F, false);

        wingLeft = new ModelRenderer(this);
        wingLeft.setRotationPoint(-1.9641F, -2.0F, 2.0F);
        wingLeft.setTextureOffset(0, 12).addBox(0.5F, -5.5F, 0.0F, 0.0F, 12.0F, 20.0F, 0.0F, false);
        wingLeft.setTextureOffset(0, 28).addBox(0.5F, 6.5F, 0.0F, 0.0F, 10.0F, 16.0F, 0.0F, false);
    }

    @Override
    protected Iterable<ModelRenderer> getHeadParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelRenderer> getBodyParts() {
        return ImmutableList.of(wingLeft, wingRight);
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float netHeadYaw, float headPitch) {
        float f = ((float) entityIn.getMotion().length()) * 10;
        ticks += Minecraft.getInstance().getTickLength() * (1 + Math.min(9, f * f * f)) * 0.1f;
        wingLeft.rotateAngleY = -1.0472F + MathHelper.sin(ticks) * 0.25f;
        wingRight.rotateAngleY = -wingLeft.rotateAngleY;
    }

}

package snownee.kiwi.contributor.impl.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

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
        textureWidth = 48;
        textureHeight = 32;

        this.ear1 = new ModelRenderer(this, 8, 1);
        this.ear1.addBox(-4.0F, -10.0F, -4.0F, 2.0F, 2.0F, 1.0F);
        this.ear2 = new ModelRenderer(this, 15, 1);
        this.ear2.addBox(2.0F, -10.0F, -4.0F, 2.0F, 2.0F, 1.0F);
        this.tail = new ModelRenderer(this, 30, 0);
        this.tail.addBox(0F, 0F, 0F, 4.0F, 9.0F, 5.0F);
    }

    @Override
    protected Iterable<ModelRenderer> getHeadParts() {
        return ImmutableList.of(ear1, ear2);
    }

    @Override
    protected Iterable<ModelRenderer> getBodyParts() {
        return ImmutableList.of(tail);
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        ear1.copyModelAngles(playerModel.bipedHead);
        ear2.copyModelAngles(playerModel.bipedHead);
        if (ageInTicks % 60 < 2) {
            ear1.rotateAngleY += 0.05f;
            ear2.rotateAngleY -= 0.05f;
        }
        float delta = MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        if (entityIn.isCrouching()) {
            this.tail.setRotationPoint(-2.0F, 14.0F, 5.5F);
            this.tail.rotateAngleX = 1.25F + delta;
        } else {
            this.tail.setRotationPoint(-2.0F, 10.0F, .5F);
            this.tail.rotateAngleX = 0.85F + delta;
        }

    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.getHeadParts().forEach((p_228228_8_) -> {
            p_228228_8_.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        });
        this.getBodyParts().forEach((p_228227_8_) -> {
            p_228227_8_.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        });
    }

}

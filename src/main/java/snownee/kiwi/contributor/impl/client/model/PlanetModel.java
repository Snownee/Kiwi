package snownee.kiwi.contributor.impl.client.model;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlanetModel<T extends LivingEntity> extends AgeableModel<T> {

	private final ModelRenderer largePlanet;
	private final ModelRenderer smallPlanet;

	public PlanetModel() {
		texWidth = 32;
		texHeight = 32;

		largePlanet = new ModelRenderer(this);
		largePlanet.setPos(0.0F, 20.0F, -20.0F);
		largePlanet.texOffs(0, 0).addBox(-2.0F, -36.0F, -2.0F, 4.0F, 4.0F, 4.0F, 0.0F, false);
		largePlanet.texOffs(-9, 8).addBox(-4.5F, -34.0F, -4.5F, 9.0F, 0.0F, 9.0F, 0.0F, false);
		smallPlanet = new ModelRenderer(this);
		smallPlanet.setPos(0.0F, 20.0F, 16.0F);
		smallPlanet.texOffs(16, 0).addBox(-1.5F, -35.0F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);
	}

	@Override
	protected Iterable<ModelRenderer> headParts() {
		return ImmutableList.of();
	}

	@Override
	protected Iterable<ModelRenderer> bodyParts() {
		return ImmutableList.of(largePlanet, smallPlanet);
	}

	@Override
	public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		largePlanet.yRot = -ageInTicks / 10;
		smallPlanet.yRot = -ageInTicks / 6;
	}

}

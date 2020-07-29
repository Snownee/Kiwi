// Made with Blockbench 3.6.3
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports


public class PlanetModel extends EntityModel<Entity> {
	private final ModelRenderer largePlanet;
	private final ModelRenderer bb_main;

	public PlanetModel() {
		textureWidth = 32;
		textureHeight = 32;

		largePlanet = new ModelRenderer(this);
		largePlanet.setRotationPoint(0.0F, 24.0F, 0.0F);
		largePlanet.setTextureOffset(0, 0).addBox(-2.0F, -36.0F, -2.0F, 4.0F, 4.0F, 4.0F, 0.0F, false);
		largePlanet.setTextureOffset(-9, 8).addBox(-4.5F, -34.0F, -4.5F, 9.0F, 0.0F, 9.0F, 0.0F, false);

		bb_main = new ModelRenderer(this);
		bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
		bb_main.setTextureOffset(16, 0).addBox(-1.5F, -35.0F, -1.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		largePlanet.render(matrixStack, buffer, packedLight, packedOverlay);
		bb_main.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
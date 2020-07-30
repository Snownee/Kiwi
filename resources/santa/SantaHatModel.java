// Made with Blockbench 3.6.3
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports


public class SantaHatModel extends EntityModel<Entity> {
	private final ModelRenderer bb_main;

	public SantaHatModel() {
		textureWidth = 32;
		textureHeight = 32;

		bb_main = new ModelRenderer(this);
		bb_main.setRotationPoint(0.0F, 24.0F, 0.0F);
		bb_main.setTextureOffset(0, 8).addBox(-0.5F, -10.0F, -3.0F, 5.0F, 2.0F, 5.0F, 0.0F, false);
		bb_main.setTextureOffset(0, 0).addBox(0.0F, -12.0F, -2.5F, 4.0F, 3.0F, 4.0F, 0.0F, false);
		bb_main.setTextureOffset(16, 4).addBox(4.0F, -12.5F, -1.5F, 2.0F, 4.0F, 2.0F, 0.0F, false);
		bb_main.setTextureOffset(12, 0).addBox(5.0F, -9.0F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		bb_main.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
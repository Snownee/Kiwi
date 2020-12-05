// Made with Blockbench 3.7.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports


public class SunnyMilkWing extends EntityModel<Entity> {
	private final ModelRenderer wingRight;
	private final ModelRenderer wingLeft;

	public SunnyMilkWing() {
		textureWidth = 64;
		textureHeight = 64;

		wingRight = new ModelRenderer(this);
		wingRight.setRotationPoint(1.9641F, 22.0F, 5.0F);
		wingRight.setTextureOffset(0, 12).addBox(-0.5F, -5.5F, 0.0F, 0.0F, 12.0F, 20.0F, 0.0F, false);
		wingRight.setTextureOffset(0, 28).addBox(0.5F, 6.5F, 0.0F, 0.0F, 10.0F, 16.0F, 0.0F, false);

		wingLeft = new ModelRenderer(this);
		wingLeft.setRotationPoint(-1.9641F, 22.0F, 5.0F);
		wingLeft.setTextureOffset(0, 12).addBox(0.5F, -5.5F, 0.0F, 0.0F, 12.0F, 20.0F, 0.0F, false);
		wingLeft.setTextureOffset(0, 28).addBox(0.5F, 6.5F, 0.0F, 0.0F, 10.0F, 16.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		wingRight.render(matrixStack, buffer, packedLight, packedOverlay);
		wingLeft.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
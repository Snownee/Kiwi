package snownee.kiwi.customization.shape;

import java.util.stream.Stream;

import net.minecraft.resources.ResourceLocation;

public class ShapeRef implements UnbakedShape {
	private final ResourceLocation id;
	private ShapeGenerator baked;

	public ShapeRef(ResourceLocation id) {
		this.id = id;
	}

	public ResourceLocation id() {
		return id;
	}

	@Override
	public ShapeGenerator bake(BakingContext context) {
		return baked;
	}

	@Override
	public Stream<UnbakedShape> dependencies() {
		return Stream.empty();
	}

	public boolean isResolved() {
		return baked != null;
	}

	public boolean bindValue(BakingContext context) {
		baked = context.getShape(id);
		return baked != null;
	}

//	@Override
//	public int hashCode() {
//		return id.hashCode();
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) {
//			return true;
//		}
//		if (obj instanceof ShapeRef) {
//			return id.equals(((ShapeRef) obj).id);
//		}
//		return false;
//	}
}

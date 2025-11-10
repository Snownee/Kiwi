package snownee.kiwi.customization.shape;

import java.util.List;

import net.minecraft.util.StringRepresentable;

public enum BlockShapeType implements StringRepresentable {
	MAIN("main"),
	COLLISION("collision"),
	INTERACTION("interaction");

	public static final List<BlockShapeType> VALUES = List.of(values());

	private final String name;

	BlockShapeType(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}

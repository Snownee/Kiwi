package snownee.kiwi.customization.builder;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public enum FacingLimitation implements StringRepresentable {
	None("none"),
	FrontAndBack("front_and_back"),
	Side("side");

	private final String name;

	FacingLimitation(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public boolean test(Direction direction1, Direction direction2) {
		return switch (this) {
			case None -> true;
			case FrontAndBack -> direction1.getAxis() == direction2.getAxis();
			case Side -> direction1.getAxis() != direction2.getAxis();
		};
	}
}

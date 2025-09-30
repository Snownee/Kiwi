package snownee.kiwi.customization.block.component;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

public interface LayeredComponent {
	IntegerProperty getLayerProperty();

	int getDefaultLayer();
}

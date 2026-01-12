package snownee.kiwi.customization.shape;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.shapes.Shapes;
import snownee.kiwi.Kiwi;

public interface BakingContext {
	ShapeGenerator getShape(Identifier id);

	class Impl implements BakingContext {
		public final Map<Identifier, ShapeGenerator> byId;
		private final ShapeGenerator fallbackShape;

		public Impl(Map<Identifier, UnbakedShape> unbaked) {
			byId = Maps.newHashMapWithExpectedSize(unbaked.size());
			fallbackShape = ShapeGenerator.unit(Shapes.block());
		}

		@Override
		public ShapeGenerator getShape(Identifier id) {
			return Preconditions.checkNotNull(byId.get(id), "Shape not found: %s", id);
		}

		public void bake(Identifier id, UnbakedShape unbaked) {
			ShapeGenerator baked;
			try {
				baked = unbaked.bake(this);
			} catch (Exception e) {
				Kiwi.LOGGER.error("Failed to bake shape: %s".formatted(id), e);
				baked = fallbackShape;
			}
			byId.put(id, baked);
		}
	}
}

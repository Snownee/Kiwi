package snownee.kiwi.customization.shape;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.shapes.Shapes;
import snownee.kiwi.Kiwi;

public interface BakingContext {
	ShapeGenerator getShape(ResourceLocation id);

	class Impl implements BakingContext {
		public final Map<ResourceLocation, ShapeGenerator> byId;
		private final ShapeGenerator fallbackShape;

		public Impl(Map<ResourceLocation, UnbakedShape> unbaked) {
			byId = Maps.newHashMapWithExpectedSize(unbaked.size());
			fallbackShape = ShapeGenerator.unit(Shapes.block());
		}

		@Override
		public ShapeGenerator getShape(ResourceLocation id) {
			return Preconditions.checkNotNull(byId.get(id), "Shape not found: %s", id);
		}

		public void bake(ResourceLocation id, UnbakedShape unbaked) {
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

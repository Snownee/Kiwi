package snownee.kiwi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface RenderLayer {
	Layer value();

	enum Layer {
		CUTOUT_MIPPED(() -> RenderType::cutoutMipped),
		CUTOUT(() -> RenderType::cutout),
		TRANSLUCENT(() -> RenderType::translucent);

		private final Supplier<Supplier<RenderType>> supplier;

		Layer(Supplier<Supplier<RenderType>> supplier) {
			this.supplier = supplier;
		}

		@OnlyIn(Dist.CLIENT)
		public RenderType get() {
			return supplier.get().get();
		}
	}
}

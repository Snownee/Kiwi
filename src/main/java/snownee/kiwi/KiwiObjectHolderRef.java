package snownee.kiwi;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import snownee.kiwi.util.ReflectionUtil;

public class KiwiObjectHolderRef implements Consumer<Predicate<ResourceLocation>> {

	private final Field field;
	private final ResourceLocation injectedObject;
	private final IForgeRegistry<?> registry;

	public KiwiObjectHolderRef(@Nullable Field field, ResourceLocation injectedObject, IForgeRegistry<?> registry) {
		this.field = field;
		this.injectedObject = injectedObject;
		this.registry = registry;
	}

	@Override
	public void accept(Predicate<ResourceLocation> filter) {
		if (field == null || !filter.test(registry.getRegistryName()))
			return;

		if (!registry.containsKey(injectedObject)) {
			Kiwi.logger.debug("Unable to lookup {} for {}. This means the object wasn't registered. It's likely just mod options.", injectedObject, field);
			return;
		}

		Object thing = registry.getValue(injectedObject);
		try {
			ReflectionUtil.setFinalValue(field, null, thing);
		} catch (Exception e) {
			Kiwi.logger.warn("Unable to set {} with value {} ({})", this.field, thing, this.injectedObject, e);
		}
	}

	public KiwiObjectHolderRef withField(Field field) {
		return new KiwiObjectHolderRef(field, injectedObject, registry);
	}

	public Class<?> getRegistryType() {
		return registry.getRegistrySuperType();
	}

	@Override
	public int hashCode() {
		if (field == null) {
			return registry.hashCode() * 31 + injectedObject.hashCode();
		} else {
			return field.hashCode();
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof KiwiObjectHolderRef))
			return false;
		KiwiObjectHolderRef o = (KiwiObjectHolderRef) other;
		if (field == null) {
			return this.registry.equals(o.registry) && this.injectedObject.equals(o.injectedObject);
		} else {
			return this.field.equals(o.field);
		}
	}
}

package snownee.kiwi.recipe;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.Kiwi;

public record ModuleLoadedCondition(ResourceLocation module) implements ResourceCondition {
	public static final ResourceConditionType<ModuleLoadedCondition> TYPE = ResourceConditionType.create(
			Kiwi.id("is_loaded"),
			RecordCodecBuilder.mapCodec(instance ->
					instance.group(ResourceLocation.CODEC.fieldOf("module").forGetter(ModuleLoadedCondition::module))
							.apply(instance, ModuleLoadedCondition::new))
	);

	@Override
	public ResourceConditionType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean test(@Nullable HolderLookup.Provider registryLookup) {
		return Kiwi.isLoaded(module);
	}
}

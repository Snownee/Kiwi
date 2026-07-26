package snownee.kiwi.data;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import snownee.kiwi.Kiwi;

public record NeoConditionWrapper(Type type, ICondition condition) implements ResourceCondition {
	public static void wrapAll() {
		for (Holder<MapCodec<? extends ICondition>> holder : NeoForgeRegistries.CONDITION_SERIALIZERS.asHolderIdMap()) {
			Identifier id = holder.unwrapKey().orElseThrow().identifier();
			//noinspection ConstantValue
			if ("neoforge".equals(id.getNamespace()) || ResourceConditions.getConditionType(id) != null) {
				continue;
			}
			Kiwi.LOGGER.info("Registering NeoConditionWrapper: {}", id);
			//noinspection unchecked,rawtypes
			ResourceConditions.register(new NeoConditionWrapper.Type((Holder) (Object) holder));
		}
	}

	@Override
	public ResourceConditionType<?> getType() {
		return type;
	}

	@Override
	public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryInfo) {
		return condition.test(ICondition.IContext.EMPTY);
	}

	public static class Type implements ResourceConditionType<NeoConditionWrapper> {
		private final Identifier id;
		private final MapCodec<NeoConditionWrapper> codec;

		public Type(Holder<MapCodec<ICondition>> holder) {
			this.id = holder.unwrapKey().orElseThrow().identifier();
			this.codec = holder.value().xmap(condition -> new NeoConditionWrapper(this, condition), wrapper -> wrapper.condition);
		}

		@Override
		public Identifier id() {
			return id;
		}

		@Override
		public MapCodec<NeoConditionWrapper> codec() {
			return codec;
		}
	}
}

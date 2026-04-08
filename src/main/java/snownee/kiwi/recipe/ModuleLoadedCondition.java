package snownee.kiwi.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.conditions.ICondition;
import snownee.kiwi.Kiwi;

public record ModuleLoadedCondition(Identifier module) implements ICondition {
	public static final MapCodec<ModuleLoadedCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Identifier.CODEC.fieldOf("module").forGetter(ModuleLoadedCondition::module)
	).apply(instance, ModuleLoadedCondition::new));

	@Override
	public boolean test(IContext ctx) {
		return Kiwi.isLoaded(module);
	}

	@Override
	public MapCodec<? extends ICondition> codec() {
		return CODEC;
	}
}

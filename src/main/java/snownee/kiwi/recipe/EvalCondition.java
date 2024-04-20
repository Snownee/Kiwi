package snownee.kiwi.recipe;

import org.jetbrains.annotations.Nullable;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.KEval;

public record EvalCondition(String expression) implements ResourceCondition {
	public static final ResourceConditionType<EvalCondition> TYPE = ResourceConditionType.create(
			new ResourceLocation(Kiwi.ID, "eval"),
			RecordCodecBuilder.mapCodec(instance ->
					instance.group(Codec.STRING.fieldOf("ex").forGetter(EvalCondition::expression)).apply(instance, EvalCondition::new))
	);

	public static final ResourceLocation ID = new ResourceLocation(Kiwi.ID, "eval");

	@Override
	public ResourceConditionType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean test(@Nullable HolderLookup.Provider registryLookup) {
		try {
			return new Expression(expression, KEval.config()).evaluate().getBooleanValue();
		} catch (EvaluationException | ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
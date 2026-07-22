package snownee.kiwi.recipe;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.neoforged.neoforge.common.conditions.ICondition;
import snownee.kiwi.util.KEval;

public record EvalCondition(String expression) implements ICondition {
	public static final MapCodec<EvalCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.fieldOf("ex").forGetter(EvalCondition::expression)
	).apply(instance, EvalCondition::new));

	@Override
	public boolean test(IContext context) {
		try {
			return new Expression(expression, KEval.config()).evaluate().getBooleanValue();
		} catch (EvaluationException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MapCodec<? extends ICondition> codec() {
		return CODEC;
	}
}

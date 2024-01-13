package snownee.kiwi.util;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.ToIntFunction;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.DataAccessorIfc;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.operators.AbstractOperator;
import com.ezylang.evalex.operators.InfixOperator;
import com.ezylang.evalex.operators.OperatorIfc;
import com.ezylang.evalex.parser.Token;

import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.KiwiCommonConfig;
import snownee.kiwi.KiwiModules;
import snownee.kiwi.loader.Platform;

public class KEval {

	private static final ExpressionConfiguration CONFIG = ExpressionConfiguration.builder()
			.defaultConstants(generateConstants())
			.dataAccessorSupplier(DataAccessor::new)
			.build();

	static {
		config().getFunctionDictionary().addFunction("HAS", new HasFunction());
		config().getFunctionDictionary().addFunction("VER", new VerFunction());
		config().getOperatorDictionary().addOperator("??", new NullishCoalescingOperator());
	}

	private static Map<String, EvaluationValue> generateConstants() {
		Map<String, EvaluationValue> map = new TreeMap<>(ExpressionConfiguration.StandardConstants);
		map.put("MC", EvaluationValue.numberValue(new BigDecimal(Platform.getVersionNumber("minecraft"))));
		map.put("FORGELIKE", EvaluationValue.booleanValue(Platform.getPlatformSeries() == Platform.Type.Forge));
		map.put("FABRICLIKE", EvaluationValue.booleanValue(Platform.getPlatformSeries() == Platform.Type.Fabric));
		map.put("MODLOADER", EvaluationValue.stringValue(Platform.getPlatform().name()));
		return map;
	}

	public static ExpressionConfiguration config() {
		return CONFIG;
	}

	@FunctionParameter(name = "id")
	private static class HasFunction extends AbstractFunction {
		@Override
		public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues) throws EvaluationException {
			String string = parameterValues[0].getStringValue();
			if (string.startsWith("@")) {
				return EvaluationValue.booleanValue(KiwiModules.isLoaded(new ResourceLocation(string.substring(1))));
			} else {
				return EvaluationValue.booleanValue(Platform.isModLoaded(string));
			}
		}
	}

	@FunctionParameter(name = "id")
	private static class VerFunction extends AbstractFunction {
		private final Object2IntMap<String> cache = new Object2IntAVLTreeMap<>();

		@Override
		public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues) throws EvaluationException {
			String string = parameterValues[0].getStringValue();
			return EvaluationValue.numberValue(new BigDecimal(cache.computeIfAbsent(string, (ToIntFunction<String>) Platform::getVersionNumber)));
		}
	}

	private static class DataAccessor implements DataAccessorIfc {
		@Override
		public EvaluationValue getData(String variable) {
			Object o = KiwiCommonConfig.vars.get(variable);
			return new EvaluationValue(o, config());
		}

		@Override
		public void setData(String variable, EvaluationValue value) {
		}
	}

	@InfixOperator(precedence = OperatorIfc.OPERATOR_PRECEDENCE_OR)
	private static class NullishCoalescingOperator extends AbstractOperator {

		@Override
		public EvaluationValue evaluate(Expression expression, Token operatorToken, EvaluationValue... operands) throws EvaluationException {
			if (operands[0].isNullValue()) {
				return operands[1];
			} else {
				return operands[0];
			}
		}
	}
}

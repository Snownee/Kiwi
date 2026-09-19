package snownee.kiwi.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import com.ezylang.evalex.parser.ASTNode;
import com.ezylang.evalex.parser.Token;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import net.minecraft.resources.Identifier;
import snownee.kiwi.KiwiCommonConfig;
import snownee.kiwi.KiwiModules;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.loader.Platform;

public class KEval {

	private static final ExpressionConfiguration CONFIG = ExpressionConfiguration.builder()
			.defaultConstants(generateConstants())
			.dataAccessorSupplier(() -> DataAccessor.INSTANCE)
			.singleQuoteStringLiteralsAllowed(true)
			.build();

	static {
		config().getFunctionDictionary().addFunction("HAS", new HasFunction());
		config().getFunctionDictionary().addFunction("VER", new VerFunction());
		config().getFunctionDictionary().addFunction("CFG", new CfgFunction());
		config().getFunctionDictionary().addFunction("RESET", new ResetFunction());
		config().getOperatorDictionary().addOperator("=", new AssignmentOperator());
		config().getOperatorDictionary().addOperator("??", new NullishCoalescingOperator());
	}

	private static Map<String, EvaluationValue> generateConstants() {
		Map<String, EvaluationValue> map = new TreeMap<>(ExpressionConfiguration.StandardConstants);
		map.put("MC", versionArray(Platform.getVersionNumber(Identifier.DEFAULT_NAMESPACE)));
		map.put("DEVENV", EvaluationValue.booleanValue(!Platform.isProduction()));
		map.put("ISCLIENT", EvaluationValue.booleanValue(Platform.isPhysicalClient()));
		map.put("MODLOADER", EvaluationValue.stringValue(Platform.getPlatform().name()));
		return map;
	}

	private static EvaluationValue versionArray(int[] version) {
		List<EvaluationValue> values = new ArrayList<>(version.length);
		for (int part : version) {
			values.add(EvaluationValue.numberValue(BigDecimal.valueOf(part)));
		}
		return EvaluationValue.arrayValue(values);
	}

	public static ExpressionConfiguration config() {
		return CONFIG;
	}

	@FunctionParameter(name = "id")
	private static class HasFunction extends AbstractFunction {
		@Override
		public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues) {
			String string = parameterValues[0].getStringValue();
			if (string.startsWith("@")) {
				return EvaluationValue.booleanValue(KiwiModules.isLoaded(Identifier.parse(string.substring(1))));
			} else {
				return EvaluationValue.booleanValue(Platform.isModLoaded(string));
			}
		}
	}

	@FunctionParameter(name = "id")
	private static class VerFunction extends AbstractFunction {
		private final Map<String, EvaluationValue> cache = Maps.newHashMap();

		@Override
		public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues) {
			String s = parameterValues[0].getStringValue();
			if (!Platform.isModLoaded(s)) {
				return EvaluationValue.NULL_VALUE;
			}
			return cache.computeIfAbsent(s, id -> versionArray(Platform.getVersionNumber(id)));
		}
	}

	@FunctionParameter(name = "path")
	private static class CfgFunction extends AbstractFunction {
		@Override
		public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues) {
			String path = parameterValues[0].getStringValue();
			ConfigHandler.Value<?> value = KiwiConfigManager.getValue(path);
			if (value == null) {
				return EvaluationValue.NULL_VALUE;
			}
			Object raw = value.get();
			try {
				return EvaluationValue.of(raw, expression.getConfiguration());
			} catch (Exception e) {
				return EvaluationValue.stringValue(String.valueOf(raw));
			}
		}
	}

	private static class ResetFunction extends AbstractFunction {
		@Override
		public EvaluationValue evaluate(Expression expression, Token functionToken, EvaluationValue... parameterValues) {
			((DataAccessor) expression.getDataAccessor()).variables.clear();
			return EvaluationValue.stringValue("Variables reset");
		}
	}

	private static class DataAccessor implements DataAccessorIfc {
		static final DataAccessor INSTANCE = new DataAccessor();
		private final Map<String, EvaluationValue> variables = new TreeMap<>();

		@Override
		public EvaluationValue getData(String variable) {
			if (KiwiCommonConfig.vars.containsKey(variable)) {
				return EvaluationValue.of(KiwiCommonConfig.vars.get(variable), config());
			}
			return variables.get(variable);
		}

		@Override
		public void setData(String variable, EvaluationValue value) {
			if (KiwiCommonConfig.vars.containsKey(variable)) {
				throw new IllegalArgumentException("Cannot assign to constant");
			}
			variables.put(variable, value);
		}
	}

	@InfixOperator(precedence = -1, operandsLazy = true)
	private static class AssignmentOperator extends AbstractOperator {
		@Override
		public EvaluationValue evaluate(
				Expression expression,
				Token operatorToken,
				EvaluationValue... operands) throws EvaluationException {
			List<ASTNode> parameters;
			try {
				parameters = expression.getAbstractSyntaxTree().getParameters();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			Preconditions.checkArgument(parameters.size() == 2, "Assignment operator must have exactly two operands");
			ASTNode left = parameters.getFirst();
			Preconditions.checkArgument(
					left.getToken().getType() == Token.TokenType.VARIABLE_OR_CONSTANT,
					"Left side of assignment must be a variable");
			String varName = left.getToken().getValue();
			if (expression.getConstants().containsKey(varName)) {
				throw new IllegalArgumentException("Cannot assign to constant");
			}
			EvaluationValue value = expression.evaluateSubtree(operands[1].getExpressionNode());
			expression.getDataAccessor().setData(varName, value);
			return value;
		}
	}

	@InfixOperator(precedence = OperatorIfc.OPERATOR_PRECEDENCE_OR, operandsLazy = true)
	private static class NullishCoalescingOperator extends AbstractOperator {
		@Override
		public EvaluationValue evaluate(Expression expression, Token operatorToken, EvaluationValue... operands) {
			if (operands[0].isNullValue()) {
				return operands[1];
			} else {
				return operands[0];
			}
		}
	}
}

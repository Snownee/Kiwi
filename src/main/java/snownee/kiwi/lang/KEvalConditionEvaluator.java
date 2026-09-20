package snownee.kiwi.lang;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.parser.ParseException;

import snownee.kiwi.util.KEval;

public enum KEvalConditionEvaluator implements ConditionEvaluator {
	INSTANCE;

	@Override
	public boolean test(String expression) {
		try {
			return new Expression(expression, KEval.config()).evaluate().getBooleanValue();
		} catch (EvaluationException | ParseException e) {
			throw new IllegalArgumentException("Failed to evaluate condition: " + expression, e);
		}
	}

	@Override
	public String evaluate(String expression) {
		try {
			EvaluationValue value = new Expression(expression, KEval.config()).evaluate();
			if (value.isNullValue()) {
				return "";
			}
			if (value.isNumberValue()) {
				return value.getNumberValue().stripTrailingZeros().toPlainString();
			}
			return String.valueOf(value.getValue());
		} catch (EvaluationException | ParseException e) {
			throw new IllegalArgumentException("Failed to evaluate expression: " + expression, e);
		}
	}

	@Override
	public boolean isReservedWord(String name) {
		var config = KEval.config();
		if (config.getFunctionDictionary().hasFunction(name)) {
			return true;
		}
		for (String constant : config.getDefaultConstants().keySet()) {
			if (constant.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
}

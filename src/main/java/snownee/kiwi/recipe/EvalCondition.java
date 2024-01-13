package snownee.kiwi.recipe;

import java.util.function.Predicate;

import com.ezylang.evalex.Expression;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.KEval;

public enum EvalCondition implements Predicate<JsonObject> {
	INSTANCE;

	public static final ResourceLocation ID = new ResourceLocation(Kiwi.MODID, "eval");

	public static Provider provider(String expression) {
		return new Provider(expression);
	}

	@Override
	public boolean test(JsonObject jsonObject) {
		try {
			String ex = GsonHelper.getAsString(jsonObject, "ex");
			return new Expression(ex, KEval.config()).evaluate().getBooleanValue();
		} catch (Throwable e) {
			throw new JsonSyntaxException(e);
		}
	}

	public static class Provider implements ConditionJsonProvider {
		private final String expression;

		protected Provider(String expression) {
			this.expression = expression;
		}

		@Override
		public void writeParameters(JsonObject json) {
			json.addProperty("ex", expression);
		}

		@Override
		public ResourceLocation getConditionId() {
			return EvalCondition.ID;
		}
	}

}
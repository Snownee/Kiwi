package snownee.kiwi.recipe;

import com.ezylang.evalex.Expression;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.KEval;

public class EvalCondition implements ICondition {
	public static final ResourceLocation ID = Kiwi.id("eval");

	private final String expression;

	public EvalCondition(String expression) {
		this.expression = expression;
	}

	@Override
	public ResourceLocation getID() {
		return ID;
	}

	@Override
	public boolean test(IContext context) {
		try {
			return new Expression(expression, KEval.config()).evaluate().getBooleanValue();
		} catch (Throwable e) {
			throw new JsonSyntaxException(e);
		}
	}

	public enum Serializer implements IConditionSerializer<EvalCondition> {
		INSTANCE;

		@Override
		public void write(JsonObject json, EvalCondition value) {
			json.addProperty("ex", value.expression);
		}

		@Override
		public EvalCondition read(JsonObject json) {
			return new EvalCondition(GsonHelper.getAsString(json, "ex"));
		}

		@Override
		public ResourceLocation getID() {
			return EvalCondition.ID;
		}
	}

}
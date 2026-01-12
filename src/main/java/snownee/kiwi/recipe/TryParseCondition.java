/*
package snownee.kiwi.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import snownee.kiwi.Kiwi;

public class TryParseCondition implements ICondition {

	private static final Identifier NAME = new Identifier(Kiwi.MODID, "try_parse");

	private final JsonElement e;

	public TryParseCondition(JsonElement e) {
		this.e = e;
	}

	@Override
	public Identifier getID() {
		return NAME;
	}

	@Override
	public boolean test() {
		try {
			Ingredient ingredient = AlternativesIngredientSerializer.getIngredient(e);
			return !ingredient.isEmpty();
		} catch (Exception e) {
			return false;
		}
	}

	public enum Serializer implements IConditionSerializer<TryParseCondition> {
		INSTANCE;

		@Override
		public void write(JsonObject json, TryParseCondition condition) {
			json.add("value", condition.e);
		}

		@Override
		public TryParseCondition read(JsonObject json) {
			return new TryParseCondition(json.get("value"));
		}

		@Override
		public Identifier getID() {
			return NAME;
		}

	}

}
*/
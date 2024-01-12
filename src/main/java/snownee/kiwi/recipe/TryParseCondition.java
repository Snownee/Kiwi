package snownee.kiwi.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import snownee.kiwi.Kiwi;

@Deprecated
public class TryParseCondition implements ICondition {

	private static final ResourceLocation ID = new ResourceLocation(Kiwi.ID, "try_parse");

	private final JsonElement e;

	public TryParseCondition(JsonElement e) {
		this.e = e;
	}

	@Override
	public ResourceLocation getID() {
		return ID;
	}

	@Override
	public boolean test(IContext ctx) {
		try {
			Ingredient ingredient = getIngredient(e, ctx);
			return !ingredient.isEmpty();
		} catch (Exception e) {
			return false;
		}
	}

	public static Ingredient getIngredient(JsonElement e, IContext ctx) {
		if (e.isJsonObject()) {
			JsonObject o = e.getAsJsonObject();
			if (o.size() == 1 && o.has("tag")) {
				ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(o, "tag"));
				TagKey<Item> tagkey = TagKey.create(Registries.ITEM, resourcelocation);
				if (ctx.getTag(tagkey).isEmpty()) {
					throw new JsonSyntaxException("hasNoMatchingItems");
				}
			}
		}
		return CraftingHelper.getIngredient(e, false);
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
		public ResourceLocation getID() {
			return ID;
		}

	}

}

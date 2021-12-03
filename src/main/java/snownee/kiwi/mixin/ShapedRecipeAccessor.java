package snownee.kiwi.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccessor {

	@Invoker
	static Map<String, Ingredient> callKeyFromJson(JsonObject p_44211_) {
		throw new IllegalStateException();
	}

	@Invoker
	static String[] callPatternFromJson(JsonArray p_44197_) {
		throw new IllegalStateException();
	}

	@Invoker
	static String[] callShrink(String... p_44187_) {
		throw new IllegalStateException();
	}

	@Invoker
	static NonNullList<Ingredient> callDissolvePattern(String[] p_44203_, Map<String, Ingredient> p_44204_, int p_44205_, int p_44206_) {
		throw new IllegalStateException();
	}

}

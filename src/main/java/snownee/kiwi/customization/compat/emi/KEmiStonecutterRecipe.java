/*
package snownee.kiwi.customization.compat.emi;

import java.util.List;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;

public class KEmiStonecutterRecipe implements EmiRecipe {
	private final ResourceLocation id;
	private final EmiIngredient input;
	private final EmiStack output;

	public KEmiStonecutterRecipe(RecipeHolder<StonecutterRecipe> recipeHolder) {
		id = recipeHolder.id().location();
		input = EmiIngredient.of(recipeHolder.value().input());
		output = EmiStack.of(recipeHolder.value().result());
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.STONECUTTING;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(input);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(output);
	}

	@Override
	public int getDisplayWidth() {
		return 76;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 26, 1);
		widgets.addSlot(input, 0, 0);
		widgets.addSlot(output, 58, 0).recipeContext(this);
	}
}
*/

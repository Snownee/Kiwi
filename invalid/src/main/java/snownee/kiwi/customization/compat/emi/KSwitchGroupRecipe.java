package snownee.kiwi.customization.compat.emi;

import java.util.List;

import dev.emi.emi.api.recipe.EmiIngredientRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.Identifier;
import snownee.kiwi.customization.block.family.BlockFamily;
import snownee.kiwi.util.KHolder;

public class KSwitchGroupRecipe extends EmiIngredientRecipe {
	private final EmiIngredient ingredient;
	private final List<EmiStack> stacks;
	private final Identifier id;

	public KSwitchGroupRecipe(KHolder<BlockFamily> family) {
		ingredient = EmiIngredient.of(family.value().ingredientInViewer());
		stacks = family.value().items().map(EmiStack::of).toList();
		id = family.key().withPrefix("/");
	}

	@Override
	protected EmiIngredient getIngredient() {
		return ingredient;
	}

	@Override
	protected List<EmiStack> getStacks() {
		return stacks;
	}

	@Override
	protected EmiRecipe getRecipeContext(EmiStack stack, int offset) {
		return new EmiResolutionRecipe(ingredient, stack);
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return EMICompat.KSWITCH;
	}

	@Override
	public Identifier getId() {
		return id;
	}
}

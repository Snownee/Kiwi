package snownee.kiwi.data;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.recipe.AlternativesIngredient;
import snownee.kiwi.recipe.EvalCondition;
import snownee.kiwi.recipe.FullBlockIngredient;
import snownee.kiwi.recipe.ModuleLoadedCondition;
import snownee.kiwi.recipe.crafting.KiwiShapelessRecipe;
import snownee.kiwi.recipe.crafting.NoContainersShapedRecipe;
import snownee.kiwi.recipe.crafting.RetextureRecipe;

@KiwiModule("data")
public final class DataModule extends AbstractModule {

	public static final KiwiGO<RecipeSerializer<NoContainersShapedRecipe>> SHAPED_NO_CONTAINERS = go(NoContainersShapedRecipe.Serializer::new);
	public static final KiwiGO<RecipeSerializer<RetextureRecipe>> RETEXTURE = go(RetextureRecipe.Serializer::new);
	public static final KiwiGO<RecipeSerializer<KiwiShapelessRecipe>> SHAPELESS = go(KiwiShapelessRecipe.Serializer::new);

	@Override
	protected void addEntries() {
		ResourceConditions.register(ModuleLoadedCondition.ID, ModuleLoadedCondition.INSTANCE);
		ResourceConditions.register(EvalCondition.ID, EvalCondition.INSTANCE);
		CustomIngredientSerializer.register(AlternativesIngredient.Serializer.INSTANCE);
		CustomIngredientSerializer.register(FullBlockIngredient.SERIALIZER);
	}
}

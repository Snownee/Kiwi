package snownee.kiwi.data;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.recipe.AlternativesIngredient;
import snownee.kiwi.recipe.EvalCondition;
import snownee.kiwi.recipe.FullBlockIngredient;
import snownee.kiwi.recipe.ModuleLoadedCondition;
import snownee.kiwi.recipe.TryParseCondition;
import snownee.kiwi.recipe.crafting.KiwiShapelessRecipe;
import snownee.kiwi.recipe.crafting.NoContainersShapedRecipe;
import snownee.kiwi.recipe.crafting.RetextureRecipe;

@KiwiModule("data")
public final class DataModule extends AbstractModule {

	public static final KiwiGO<RecipeSerializer<NoContainersShapedRecipe>> SHAPED_NO_CONTAINERS = go(NoContainersShapedRecipe.Serializer::new);
	public static final KiwiGO<RecipeSerializer<RetextureRecipe>> RETEXTURE = go(RetextureRecipe.Serializer::new);
	public static final KiwiGO<RecipeSerializer<KiwiShapelessRecipe>> SHAPELESS = go(KiwiShapelessRecipe.Serializer::new);

	@Override
	protected void preInit() {
		CraftingHelper.register(ModuleLoadedCondition.Serializer.INSTANCE);
		CraftingHelper.register(TryParseCondition.Serializer.INSTANCE);
		CraftingHelper.register(EvalCondition.Serializer.INSTANCE);
		CraftingHelper.register(FullBlockIngredient.ID, FullBlockIngredient.SERIALIZER);
		CraftingHelper.register(AlternativesIngredient.ID, AlternativesIngredient.SERIALIZER);
	}
}

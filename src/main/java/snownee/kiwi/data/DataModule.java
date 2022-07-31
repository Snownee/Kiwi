package snownee.kiwi.data;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.recipe.ModuleLoadedCondition;
import snownee.kiwi.recipe.crafting.NoContainersShapedRecipe;
import snownee.kiwi.recipe.crafting.NoContainersShapelessRecipe;
import snownee.kiwi.recipe.crafting.RetextureRecipe;

@KiwiModule("data")
public final class DataModule extends AbstractModule {

	public static final RecipeSerializer<NoContainersShapedRecipe> SHAPED_NO_CONTAINERS = new NoContainersShapedRecipe.Serializer();
	public static final RecipeSerializer<NoContainersShapelessRecipe> SHAPELESS_NO_CONTAINERS = new NoContainersShapelessRecipe.Serializer();
	public static final RecipeSerializer<RetextureRecipe> RETEXTURE = new RetextureRecipe.Serializer();

	@Override
	protected void preInit() {
		ResourceConditions.register(ModuleLoadedCondition.ID, ModuleLoadedCondition.INSTANCE);
	}
}

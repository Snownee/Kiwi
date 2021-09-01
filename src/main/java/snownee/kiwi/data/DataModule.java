package snownee.kiwi.data;

import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.crafting.NoContainersShapedRecipe;
import snownee.kiwi.crafting.NoContainersShapelessRecipe;
import snownee.kiwi.crafting.TextureBlockRecipe;

@KiwiModule("data")
public final class DataModule extends AbstractModule {

	public static final RecipeSerializer<NoContainersShapedRecipe> SHAPED_NO_CONTAINERS = new NoContainersShapedRecipe.Serializer();
	public static final RecipeSerializer<NoContainersShapelessRecipe> SHAPELESS_NO_CONTAINERS = new NoContainersShapelessRecipe.Serializer();
	public static final RecipeSerializer<TextureBlockRecipe> TEXTURE_BLOCK = new TextureBlockRecipe.Serializer();

}

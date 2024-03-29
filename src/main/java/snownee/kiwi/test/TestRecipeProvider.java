package snownee.kiwi.test;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;

public class TestRecipeProvider extends FabricRecipeProvider {

	public TestRecipeProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void buildRecipes(Consumer<FinishedRecipe> collector) {
		oneToOneConversionRecipe(collector, TestModule.FIRST_BLOCK.get(), TestModule.TEX_BLOCK.get(), null);
	}

}

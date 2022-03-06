package snownee.kiwi.test;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import snownee.kiwi.data.provider.KiwiRecipeProvider;

public class TestRecipeProvider extends KiwiRecipeProvider {

	public TestRecipeProvider(DataGenerator generator) {
		super(generator);
	}

	@Override
	protected void addRecipes(Consumer<FinishedRecipe> collector) {
		oneToOneConversionRecipe(collector, TestModule.FIRST_BLOCK.get(), TestModule.TEX_BLOCK.get(), null);
	}

}

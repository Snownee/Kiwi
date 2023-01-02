package snownee.kiwi.test;

import java.util.function.Consumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import snownee.kiwi.Kiwi;
import snownee.kiwi.datagen.provider.KiwiRecipeProvider;

public class TestRecipeProvider extends KiwiRecipeProvider {

	public TestRecipeProvider(PackOutput output) {
		super(Kiwi.ID, output);
	}

	@Override
	public void buildRecipes(Consumer<FinishedRecipe> collector) {
		oneToOneConversionRecipe(collector, TestModule.FIRST_BLOCK.get(), TestModule.TEX_BLOCK.get(), null);
	}

}

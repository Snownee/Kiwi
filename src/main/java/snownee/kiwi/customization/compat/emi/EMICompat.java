/*
package snownee.kiwi.customization.compat.emi;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import snownee.kiwi.customization.CustomizationHooks;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.block.family.BlockFamily;
import snownee.kiwi.customization.block.family.StonecutterRecipeMaker;
import snownee.kiwi.util.KHolder;

@EmiEntrypoint
public class EMICompat implements EmiPlugin {
	@Override
	public void register(EmiRegistry registry) {
		if (CustomizationHooks.isEnabled()) {
			List<RecipeHolder<StonecutterRecipe>> recipes = Lists.newArrayList();
			for (KHolder<BlockFamily> holder : BlockFamilies.all()) {
				BlockFamily family = holder.value();
				if (family.stonecutterSource().isPresent()) {
					recipes.addAll(StonecutterRecipeMaker.makeRecipes("to", holder));
				}
				if (family.stonecutterExchange()) {
					recipes.addAll(StonecutterRecipeMaker.makeRecipes("exchange_in_viewer", holder));
				}
			}
			for (RecipeHolder<StonecutterRecipe> recipe : recipes) {
				registry.addRecipe(new KEmiStonecutterRecipe(recipe));
			}
		}
	}
}
*/

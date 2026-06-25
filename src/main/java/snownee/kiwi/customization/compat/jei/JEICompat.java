package snownee.kiwi.customization.compat.jei;

import java.util.List;

import com.google.common.collect.Lists;

import me.shedaniel.rei.plugincompatibilities.api.REIPluginCompatIgnore;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.CustomizationHooks;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.block.family.BlockFamily;
import snownee.kiwi.customization.block.family.StonecutterRecipeMaker;
import snownee.kiwi.util.KHolder;

@JeiPlugin
@REIPluginCompatIgnore
public class JEICompat implements IModPlugin {
	public static final Identifier ID = Kiwi.id("customization");
	public static final IRecipeType<KSwitchGroupRecipe> KSWITCH = IRecipeType.create(Kiwi.id("kswitch"), KSwitchGroupRecipe.class);

	@Override
	public Identifier getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		if (CustomizationHooks.isEnabled()) {
			registration.addRecipeCategories(new KSwitchGroupRecipeCategory(registration.getJeiHelpers().getGuiHelper(), KSWITCH));
		}
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if (CustomizationHooks.isEnabled()) {
			List<RecipeHolder<StonecutterRecipe>> recipes = Lists.newArrayList();
			for (KHolder<BlockFamily> holder : BlockFamilies.all()) {
				BlockFamily family = holder.value();
				if (family.stonecutterFrom().isPresent()) {
					recipes.addAll(StonecutterRecipeMaker.makeRecipes("to", holder));
				}
				if (family.stonecutterExchange()) {
					recipes.addAll(StonecutterRecipeMaker.makeRecipes("exchange_in_viewer", holder));
				}
				if (family.switchAttrs().enabled()) {
					registration.addRecipes(KSWITCH, List.of(new KSwitchGroupRecipe(holder)));
				}
			}
			registration.addRecipes(RecipeTypes.STONECUTTING, recipes);
		}
	}
}

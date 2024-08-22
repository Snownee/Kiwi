package snownee.kiwi.customization.compat.jei;

import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.CustomizationHooks;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.block.family.BlockFamily;
import snownee.kiwi.customization.block.family.StonecutterRecipeMaker;
import snownee.kiwi.util.KHolder;
import snownee.kiwi.util.NotNullByDefault;

@JeiPlugin
@NotNullByDefault
public class JEICompat implements IModPlugin {
	public static final ResourceLocation ID = Kiwi.id("customization");

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if (CustomizationHooks.isEnabled()) {
			List<StonecutterRecipe> recipes = Lists.newArrayList();
			for (KHolder<BlockFamily> holder : BlockFamilies.all()) {
				BlockFamily family = holder.value();
				if (family.stonecutterSource().isPresent()) {
					recipes.addAll(StonecutterRecipeMaker.makeRecipes("to", holder));
				}
				if (family.stonecutterExchange()) {
					recipes.addAll(StonecutterRecipeMaker.makeRecipes("exchange_in_viewer", holder));
				}
			}
			// FIXME Need List<Holder<StonecutterRecipe>>
			//registration.addRecipes(RecipeTypes.STONECUTTING, recipes);
		}
	}
}

package snownee.kiwi.customization.compat.rei;

import java.util.List;

import com.google.common.collect.Lists;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.CustomizationHooks;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.block.family.BlockFamily;
import snownee.kiwi.customization.block.family.StonecutterRecipeMaker;
import snownee.kiwi.util.KHolder;

public class REICompat implements REIClientPlugin {
	public static final ResourceLocation ID = Kiwi.id("customization");

	@Override
	public void registerDisplays(DisplayRegistry registry) {
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
			for (StonecutterRecipe recipe : recipes) {
				registry.add(recipe);
			}
		}
	}
}

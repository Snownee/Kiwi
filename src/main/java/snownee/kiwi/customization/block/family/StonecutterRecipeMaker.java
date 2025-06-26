package snownee.kiwi.customization.block.family;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import snownee.kiwi.util.KHolder;

public class StonecutterRecipeMaker {

	public static List<RecipeHolder<StonecutterRecipe>> makeRecipes() {
		List<RecipeHolder<StonecutterRecipe>> recipes = Lists.newArrayList();
		for (KHolder<BlockFamily> holder : BlockFamilies.all()) {
			recipes.addAll(makeRecipes("default", holder));
		}
		return recipes;
	}

	public static List<RecipeHolder<StonecutterRecipe>> makeRecipes(String type, KHolder<BlockFamily> holder) {
		BlockFamily family = holder.value();
		if (!family.stonecutterExchange() && family.stonecutterFrom().isEmpty()) {
			return List.of();
		}
		Ingredient input = switch (type) {
			case "default" -> family.exchangeIngredient();
			case "xei" -> family.exchangeIngredientInViewer();
			default -> throw new IllegalArgumentException();
		};
		if (input.isEmpty()) {
			return List.of();
		}
		ResourceLocation prefix = holder.key().withPath("/stonecutter/%s".formatted(holder.key().getPath()));
		List<RecipeHolder<StonecutterRecipe>> recipes = Lists.newArrayList();
		recipes.addAll(family.items().map(item -> {
			int count = Mth.floor(1 / BlockFamilies.getConvertRatio(item));
			if (count < 1) {
				return null;
			}
			ItemStack itemStack = new ItemStack(item, count);
			ResourceLocation itemKey = itemStack.getItemHolder().unwrapKey().orElseThrow().location();
			var recipeId = prefix.withSuffix("/%s/%s".formatted(itemKey.getNamespace(), itemKey.getPath()));
			var recipe = new StonecutterRecipe(prefix.toString(), input, itemStack);
			return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, recipeId), recipe);
		}).filter(Objects::nonNull).toList());
		if (family.stonecutterFrom().isPresent() && family.stonecutterFromMultiplier() != 1) {
			Ingredient ingredient = Objects.requireNonNull(family.stonecutterFromIngredient());
			recipes.addAll(family.items().map(item -> {
				int count = Mth.floor(family.stonecutterFromMultiplier() / BlockFamilies.getConvertRatio(item));
				if (count < 1) {
					return null;
				}
				ItemStack itemStack = new ItemStack(item, count);
				ResourceLocation itemKey = itemStack.getItemHolder().unwrapKey().orElseThrow().location();
				var recipeId = prefix.withSuffix("/%s/%s/from".formatted(itemKey.getNamespace(), itemKey.getPath()));
				var recipe = new StonecutterRecipe(prefix.toString(), ingredient, itemStack);
				return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, recipeId), recipe);
			}).filter(Objects::nonNull).toList());
		}
		return recipes;
	}
}

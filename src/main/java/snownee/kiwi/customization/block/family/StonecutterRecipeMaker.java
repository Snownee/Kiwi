package snownee.kiwi.customization.block.family;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStackTemplate;
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

	@SuppressWarnings("NullableProblems") // false positive, remove in future versions
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
		Identifier prefix = holder.key().withPath("/stonecutter/%s".formatted(holder.key().getPath()));
		List<RecipeHolder<StonecutterRecipe>> recipes = Lists.newArrayList();
		recipes.addAll(family.items().map(item -> {
			int count = Mth.floor(1 / BlockFamilies.getConvertRatio(item));
			if (count < 1) {
				return null;
			}
			ItemStackTemplate result = new ItemStackTemplate(item, count);
			Identifier itemKey = result.typeHolder().unwrapKey().orElseThrow().identifier();
			var recipeId = prefix.withSuffix("/%s/%s".formatted(itemKey.getNamespace(), itemKey.getPath()));
			var recipe = new StonecutterRecipe(prefix.toString(), input, result);
			return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, recipeId), recipe);
		}).filter(Objects::nonNull).toList());
		if (family.stonecutterFrom().isPresent() && family.stonecutterFromMultiplier() != 1) {
			Ingredient ingredient = Objects.requireNonNull(family.stonecutterFromIngredient());
			recipes.addAll(family.items().map(item -> {
				int count = Mth.floor(family.stonecutterFromMultiplier() / BlockFamilies.getConvertRatio(item));
				if (count < 1) {
					return null;
				}
				ItemStackTemplate result = new ItemStackTemplate(item, count);
				Identifier itemKey = result.typeHolder().unwrapKey().orElseThrow().identifier();
				var recipeId = prefix.withSuffix("/%s/%s/from".formatted(itemKey.getNamespace(), itemKey.getPath()));
				var recipe = new StonecutterRecipe(prefix.toString(), ingredient, result);
				return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, recipeId), recipe);
			}).filter(Objects::nonNull).toList());
		}
		return recipes;
	}
}
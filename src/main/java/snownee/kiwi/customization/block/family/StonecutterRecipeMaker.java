package snownee.kiwi.customization.block.family;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import snownee.kiwi.KiwiCommonConfig;
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
		List<String> denylist = KiwiCommonConfig.disableStonecuttingNamespaces;
		if (!denylist.isEmpty()) {
			if (denylist.contains("*") || denylist.contains(holder.key().getNamespace())) {
				return List.of();
			}
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
			if (BlockFamilies.getMatValue(item) < BlockFamilies.BASE_MAT_VALUE) {
				return null;
			}
			int count = (int) (BlockFamilies.BASE_MAT_VALUE / BlockFamilies.getMatValue(item));
			ItemStackTemplate result = new ItemStackTemplate(item, Math.min(count, 99));
			Identifier itemKey = result.typeHolder().unwrapKey().orElseThrow().identifier();
			var recipeId = prefix.withSuffix("/%s/%s".formatted(itemKey.getNamespace(), itemKey.getPath()));
			var recipe = new StonecutterRecipe(new Recipe.CommonInfo(true), input, result);
			return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, recipeId), recipe);
		}).filter(Objects::nonNull).toList());
		if (family.stonecutterFrom().isPresent() && family.stonecutterFromMultiplier() != 1) {
			Ingredient ingredient = Objects.requireNonNull(family.stonecutterFromIngredient());
			recipes.addAll(family.items().map(item -> {
				int count = (int) (BlockFamilies.BASE_MAT_VALUE * family.stonecutterFromMultiplier() / BlockFamilies.getMatValue(item));
				if (count < 1) {
					return null;
				}
				ItemStackTemplate result = new ItemStackTemplate(item, Math.min(count, 99));
				Identifier itemKey = result.typeHolder().unwrapKey().orElseThrow().identifier();
				var recipeId = prefix.withSuffix("/%s/%s/from".formatted(itemKey.getNamespace(), itemKey.getPath()));
				var recipe = new StonecutterRecipe(new Recipe.CommonInfo(true), ingredient, result);
				return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, recipeId), recipe);
			}).filter(Objects::nonNull).toList());
		}
		return recipes;
	}
}
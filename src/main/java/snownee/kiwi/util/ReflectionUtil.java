package snownee.kiwi.util;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.loading.FMLEnvironment;
import snownee.kiwi.Kiwi;

public final class ReflectionUtil {
	private ReflectionUtil() {
	}

	public static <C extends Container, T extends Recipe<C>> Map<ResourceLocation, Recipe<C>> getRecipes(RecipeType<T> recipeTypeIn) {
		if (FMLEnvironment.dist.isClient()) {
			return Minecraft.getInstance().level.getRecipeManager().byType(recipeTypeIn);
		} else {
			return Kiwi.getServer().getRecipeManager().byType(recipeTypeIn);
		}
	}
}

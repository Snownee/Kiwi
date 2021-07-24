package snownee.kiwi.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

	public static void setFinalValue(Field field, Object obj, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		field.setAccessible(true);
		Field modifiers = field.getClass().getDeclaredField("modifiers");
		modifiers.setAccessible(true);
		modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		field.set(obj, null);
		modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
	}

	public static <C extends Container, T extends Recipe<C>> Map<ResourceLocation, Recipe<C>> getRecipes(RecipeType<T> recipeTypeIn) {
		if (FMLEnvironment.dist.isClient()) {
			return Minecraft.getInstance().level.getRecipeManager().byType(recipeTypeIn);
		} else {
			return Kiwi.getServer().getRecipeManager().byType(recipeTypeIn);
		}
	}
}

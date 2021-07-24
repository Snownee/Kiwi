package snownee.kiwi.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
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

	public static <C extends IInventory, T extends IRecipe<C>> Map<ResourceLocation, IRecipe<C>> getRecipes(IRecipeType<T> recipeTypeIn) {
		if (FMLEnvironment.dist.isClient()) {
			return Minecraft.getInstance().level.getRecipeManager().byType(recipeTypeIn);
		} else {
			return Kiwi.getServer().getRecipeManager().byType(recipeTypeIn);
		}
	}
}

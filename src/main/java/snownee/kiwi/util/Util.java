package snownee.kiwi.util;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import net.minecraftforge.registries.ForgeRegistries;

public final class Util {
	private Util() {
	}

	public static final Direction[] DIRECTIONS = Direction.values();
	public static final Direction[] HORIZONTAL_DIRECTIONS = Arrays.stream(DIRECTIONS).filter($ -> {
		return $.getAxis().isHorizontal();
	}).sorted(Comparator.comparingInt($ -> {
		return $.get2DDataValue();
	})).toArray($ -> {
		return new Direction[$];
	});
	
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###,###");
	public static final MessageFormat MESSAGE_FORMAT = new MessageFormat("{0,number,#.#}");

	public static String color(int color) {
		return String.format("\u00A7x%06x", color & 0x00FFFFFF);
	}

	public static String formatComma(long number) {
		return DECIMAL_FORMAT.format(number);
	}

	public static String formatCompact(long number) {
		int unit = 1000;
		if (number < unit) {
			return Long.toString(number);
		}
		int exp = (int) (Math.log(number) / Math.log(unit));
		if (exp - 1 >= 0 && exp - 1 < 6) {
			char pre = "kMGTPE".charAt(exp - 1);
			return MESSAGE_FORMAT.format(new Double[] { number / Math.pow(unit, exp) }) + pre;
		}
		return Long.toString(number);
	}

	public static String trimRL(ResourceLocation rl) {
		return trimRL(rl, "minecraft");
	}

	public static String trimRL(String rl) {
		return trimRL(rl, "minecraft");
	}

	/**
	 * @since 2.7.0
	 */
	public static String trimRL(ResourceLocation rl, String defaultNamespace) {
		return rl.getNamespace().equals(defaultNamespace) ? rl.getPath() : rl.toString();
	}

	/**
	 * @since 2.7.0
	 */
	public static String trimRL(String rl, String defaultNamespace) {
		if (rl.startsWith(defaultNamespace + ":")) {
			return rl.substring(defaultNamespace.length() + 1);
		} else {
			return rl;
		}
	}

	@Nullable
	public static ResourceLocation RL(@Nullable String string) {
		try {
			return ResourceLocation.tryParse(string);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @since 2.4.2
	 */
	@Nullable
	public static ResourceLocation RL(@Nullable String string, String defaultNamespace) {
		if (string != null && !string.contains(":")) {
			string = defaultNamespace + ":" + string;
		}
		return RL(string);
	}

	public static String getTextureItem(ItemStack stack, String mark) {
		NBTHelper data = NBTHelper.of(stack);
		ResourceLocation rl = RL(data.getString("BlockEntityTag.Items." + mark));
		if (rl != null) {
			Item item = ForgeRegistries.ITEMS.getValue(rl);
			if (item != null) {
				return item.getDescriptionId();
			}
		}
		return "";
	}

	@Nullable
	public static RecipeManager getRecipeManager() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server != null) {
			return server.getRecipeManager();
		} else if (FMLEnvironment.dist.isClient()) {
			ClientPacketListener connection = Minecraft.getInstance().getConnection();
			if (connection != null) {
				return connection.getRecipeManager();
			}
		}
		return null;
	}

	public static <C extends Container, T extends Recipe<C>> Map<ResourceLocation, Recipe<C>> getRecipes(RecipeType<T> recipeTypeIn) {
		RecipeManager manager = getRecipeManager();
		if (manager == null) {
			return Collections.EMPTY_MAP;
		} else {
			return getRecipeManager().byType(recipeTypeIn);
		}
	}
}

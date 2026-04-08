package snownee.kiwi.config;

import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.screens.Screen;

public record ConfigLibAttributes(
		String name,
		Function<@Nullable String, @Nullable Screen> screenFactory,
		boolean supportsList,
		boolean supportsMap,
		boolean supportsOnlyString) {
}

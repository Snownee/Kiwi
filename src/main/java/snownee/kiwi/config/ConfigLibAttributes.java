package snownee.kiwi.config;

import java.util.function.Function;

import net.minecraft.client.gui.screens.Screen;

public record ConfigLibAttributes(String name, Function<String, Screen> screenFactory,
								  boolean supportsList, boolean supportsMap, boolean supportsOnlyString) {
}

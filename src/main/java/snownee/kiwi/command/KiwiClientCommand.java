package snownee.kiwi.command;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import snownee.kiwi.Kiwi;
import snownee.kiwi.config.ClothConfigIntegration;
import snownee.kiwi.config.ConfigLibAttributes;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.loader.Platform;

public class KiwiClientCommand {
	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommandManager.literal(Kiwi.ID + "c");
		LiteralArgumentBuilder<FabricClientCommandSource> configure = ClientCommandManager.literal("configure");
		List<ConfigLibAttributes> list = Lists.newArrayList();
		if (Platform.isModLoaded("cloth-config")) {
			list.add(ClothConfigIntegration.attributes());
		}
		if (list.isEmpty()) {
			configure.executes(ctx -> {
				ctx.getSource().sendFeedback(Component.translatable("commands.kiwi.configure.install"));
				return 0;
			});
		} else {
			Set<String> addedMods = Sets.newHashSet();
			for (ConfigLibAttributes attributes : list) {
				putMods(attributes, addedMods, configure);
			}
		}
		builder.then(configure);
		dispatcher.register(builder);
	}

	private static void putMods(
			ConfigLibAttributes attributes,
			Set<String> addedMods,
			LiteralArgumentBuilder<FabricClientCommandSource> node) {
		for (String modId : KiwiConfigManager.getModsWithScreen(attributes)) {
			if (addedMods.contains(modId)) {
				continue;
			}
			addedMods.add(modId);
			node.then(ClientCommandManager.literal(modId).executes(ctx -> {
				Screen screen = attributes.screenFactory().apply(modId);
				if (screen == null) {
					ctx.getSource().sendFeedback(Component.translatable("commands.kiwi.configure.failed"));
					return 0;
				}
				Minecraft.getInstance().tell(() -> {
					Minecraft.getInstance().setScreen(screen);
				});
				return 1;
			}));
		}
	}
}

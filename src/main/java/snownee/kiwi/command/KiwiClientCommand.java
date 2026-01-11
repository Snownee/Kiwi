package snownee.kiwi.command;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiCommonConfig;
import snownee.kiwi.config.ClothConfigIntegration;
import snownee.kiwi.config.ConfigLibAttributes;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.loader.Platform;

public class KiwiClientCommand {

	public static <T> LiteralArgumentBuilder<T> create(ClientCommandContext<T> context) {
		LiteralArgumentBuilder<T> builder = context.literal(Kiwi.ID + "c");
		builder.then(context.literal("eval")
				.executes(ctx -> KiwiCommand.evalHelp(ctx.getSource(), context::sendFailure))
				.then(context.argument("expression", StringArgumentType.greedyString()).executes(ctx -> KiwiCommand.eval(
						StringArgumentType.getString(ctx, "expression"),
						KiwiCommonConfig.evalPrintExpression,
						ctx.getSource(),
						context::sendSuccess,
						context::sendFailure))));
		LiteralArgumentBuilder<T> configure = context.literal("configure");
		List<ConfigLibAttributes> list = Lists.newArrayList();
		if (Platform.isModLoaded("cloth-config")) {
			list.add(ClothConfigIntegration.attributes());
		}
		if (list.isEmpty()) {
			configure.executes(ctx -> {
				context.sendSuccess(ctx.getSource(), Component.translatable("commands.kiwi.configure.install"));
				return 0;
			});
		} else {
			Set<String> addedMods = Sets.newHashSet();
			for (ConfigLibAttributes attributes : list) {
				putMods(context, attributes, addedMods, configure);
			}
		}
		builder.then(configure);
		return builder;
	}

	private static <T> void putMods(
			ClientCommandContext<T> context,
			ConfigLibAttributes attributes,
			Set<String> addedMods,
			LiteralArgumentBuilder<T> node) {
		for (String modId : KiwiConfigManager.getModsWithScreen(attributes)) {
			if (addedMods.contains(modId)) {
				continue;
			}
			addedMods.add(modId);
			node.then(context.literal(modId).executes(ctx -> {
				Screen screen = attributes.screenFactory().apply(modId);
				if (screen == null) {
					context.sendSuccess(ctx.getSource(), Component.translatable("commands.kiwi.configure.failed"));
					return 0;
				}
				Minecraft.getInstance().schedule(() -> {
					Minecraft.getInstance().setScreen(screen);
				});
				return 1;
			}));
		}
	}

}

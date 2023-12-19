package snownee.kiwi.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import snownee.kiwi.Kiwi;
import snownee.kiwi.config.KiwiConfigManager;

public class KiwiCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(Kiwi.ID);
		/* off */
        builder.then(Commands
				.literal("debugLevelRules")
				.requires(ctx -> ctx.hasPermission(2))
				.executes(ctx -> cleanLevel(ctx.getSource()))
		);

		builder.then(Commands
				.literal("reload")
				.requires(ctx -> ctx.hasPermission(2))
				.then(Commands.argument("fileName", StringArgumentType.greedyString())
						.executes(ctx -> {
							String fileName = StringArgumentType.getString(ctx, "fileName");
							if (KiwiConfigManager.refresh(fileName)) {
								ctx.getSource().sendSuccess(() -> Component.translatable("commands.kiwi.reload.success", fileName), true);
								return 1;
							} else {
								ctx.getSource().sendFailure(Component.translatable("commands.kiwi.reload.failed", fileName));
								return 0;
							}
						})
				)
		);
        /* on */
		dispatcher.register(builder);
	}

	private static int cleanLevel(CommandSourceStack commandSourceStack) {
		Commands commands = commandSourceStack.getServer().getCommands();
		commands.performPrefixedCommand(commandSourceStack, "gamerule doDaylightCycle false");
		commands.performPrefixedCommand(commandSourceStack, "gamerule doWeatherCycle false");
		commands.performPrefixedCommand(commandSourceStack, "gamerule doMobLoot false");
		commands.performPrefixedCommand(commandSourceStack, "gamerule doMobSpawning false");
		commands.performPrefixedCommand(commandSourceStack, "difficulty peaceful");
		commands.performPrefixedCommand(commandSourceStack, "kill @e[type=!minecraft:player]");
		commands.performPrefixedCommand(commandSourceStack, "time set day");
		commands.performPrefixedCommand(commandSourceStack, "weather clear");
		commands.performPrefixedCommand(commandSourceStack, "gamerule doMobLoot true");
		return 1;
	}
}

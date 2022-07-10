package snownee.kiwi.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import snownee.kiwi.Kiwi;

public class KiwiCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(Kiwi.MODID);
		/* off */
        builder.then(Commands
                .literal("debugLevelRules")
                .requires(ctx -> ctx.hasPermission(2))
                .executes(ctx -> cleanLevel(ctx.getSource()))
        );
        /* on */
		dispatcher.register(builder);
	}

	private static int cleanLevel(CommandSourceStack commandSourceStack) {
		Commands commands = commandSourceStack.getServer().getCommands();
		commands.performCommand(commandSourceStack, "gamerule doDaylightCycle false");
		commands.performCommand(commandSourceStack, "gamerule doWeatherCycle false");
		commands.performCommand(commandSourceStack, "gamerule doMobLoot false");
		commands.performCommand(commandSourceStack, "gamerule doMobSpawning false");
		commands.performCommand(commandSourceStack, "difficulty peaceful");
		commands.performCommand(commandSourceStack, "kill @e[type=!minecraft:player]");
		commands.performCommand(commandSourceStack, "time set day");
		commands.performCommand(commandSourceStack, "weather clear");
		commands.performCommand(commandSourceStack, "gamerule doMobLoot true");
		return 1;
	}
}

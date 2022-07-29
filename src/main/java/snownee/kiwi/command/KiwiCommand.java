package snownee.kiwi.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import snownee.kiwi.Kiwi;

public class KiwiCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandSelection environmentType) {
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

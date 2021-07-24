package snownee.kiwi.command;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.network.chat.TranslatableComponent;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.LootDumper;

public class KiwiCommand {
	private static final SimpleCommandExceptionType WRONG_PATTERN_EXCEPTION = new SimpleCommandExceptionType(new TranslatableComponent("commands.kiwi.dumpLoots.wrongPattern"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandSelection environmentType) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(Kiwi.MODID);
		/* off */
        if (environmentType != CommandSelection.DEDICATED) {
            builder.then(Commands
                    .literal("dumpLoots")
                    .executes(ctx -> dumpLoots(ctx.getSource(), ".+"))
                    .then(Commands
                            .argument("pattern", StringArgumentType.greedyString())
                            .executes(ctx -> dumpLoots(ctx.getSource(), StringArgumentType.getString(ctx, "pattern")))
                    )
            );
        }

        builder.then(Commands
                .literal("debugLevelRules")
                .requires(ctx -> ctx.hasPermission(2))
                .executes(ctx -> cleanLevel(ctx.getSource()))
        );
        /* on */
		commandDispatcher.register(builder);
	}

	public static int dumpLoots(CommandSourceStack commandSourceStack, String pattern) throws CommandSyntaxException {
		try {
			Pattern p = Pattern.compile(pattern);
			int r = LootDumper.dump(p, commandSourceStack.getServer().getServerDirectory());
			if (r == 0) {
				commandSourceStack.sendFailure(new TranslatableComponent("commands.kiwi.dumpLoots.noTargets"));
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.kiwi.dumpLoots.success", r), true);
			}
			return r;
		} catch (PatternSyntaxException e) {
			throw WRONG_PATTERN_EXCEPTION.create();
		}
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

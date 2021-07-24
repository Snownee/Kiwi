package snownee.kiwi.command;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.Commands.EnvironmentType;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.LootDumper;

public class KiwiCommand {
	private static final SimpleCommandExceptionType WRONG_PATTERN_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.kiwi.dumpLoots.wrongPattern"));

	public static void register(CommandDispatcher<CommandSource> dispatcher, EnvironmentType environmentType) {
		LiteralArgumentBuilder<CommandSource> builder = Commands.literal(Kiwi.MODID);
		/* off */
        if (environmentType != EnvironmentType.DEDICATED) {
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
                .literal("debugWorldRules")
                .requires(ctx -> ctx.hasPermission(2))
                .executes(ctx -> cleanWorld(ctx.getSource()))
        );
        /* on */
		dispatcher.register(builder);
	}

	public static int dumpLoots(CommandSource source, String pattern) throws CommandSyntaxException {
		try {
			Pattern p = Pattern.compile(pattern);
			int r = LootDumper.dump(p, source.getServer().getServerDirectory());
			if (r == 0) {
				source.sendFailure(new TranslationTextComponent("commands.kiwi.dumpLoots.noTargets"));
			} else {
				source.sendSuccess(new TranslationTextComponent("commands.kiwi.dumpLoots.success", r), true);
			}
			return r;
		} catch (PatternSyntaxException e) {
			throw WRONG_PATTERN_EXCEPTION.create();
		}
	}

	private static int cleanWorld(CommandSource source) {
		Commands commands = source.getServer().getCommands();
		commands.performCommand(source, "gamerule doDaylightCycle false");
		commands.performCommand(source, "gamerule doWeatherCycle false");
		commands.performCommand(source, "gamerule doMobLoot false");
		commands.performCommand(source, "gamerule doMobSpawning false");
		commands.performCommand(source, "difficulty peaceful");
		commands.performCommand(source, "kill @e[type=!minecraft:player]");
		commands.performCommand(source, "time set day");
		commands.performCommand(source, "weather clear");
		commands.performCommand(source, "gamerule doMobLoot true");
		return 1;
	}
}

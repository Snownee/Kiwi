package snownee.kiwi;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.kiwi.util.LootDumper;

public class KiwiCommand {
    private static final SimpleCommandExceptionType WRONG_PATTERN_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.kiwi.dumpLoots.wrongPattern"));

    public static void register(CommandDispatcher<CommandSource> dispatcher, boolean integrated) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal(Kiwi.MODID);
        /* off */
        if (integrated) {
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
                .literal("cleanWorld")
                .requires(ctx -> ctx.hasPermissionLevel(2))
                .executes(ctx -> cleanWorld(ctx.getSource()))
        );
        /* on */
        dispatcher.register(builder);
    }

    public static int dumpLoots(CommandSource source, String pattern) throws CommandSyntaxException {
        try {
            Pattern p = Pattern.compile(pattern);
            int r = LootDumper.dump(p, source.getServer().getDataDirectory());
            if (r == 0) {
                source.sendErrorMessage(new TranslationTextComponent("commands.kiwi.dumpLoots.noTargets"));
            } else {
                source.sendFeedback(new TranslationTextComponent("commands.kiwi.dumpLoots.success", r), true);
            }
            return r;
        } catch (PatternSyntaxException e) {
            throw WRONG_PATTERN_EXCEPTION.create();
        }
    }

    private static int cleanWorld(CommandSource source) {
        Commands commands = source.getServer().getCommandManager();
        commands.handleCommand(source, "gamerule doDaylightCycle false");
        commands.handleCommand(source, "gamerule doWeatherCycle false");
        commands.handleCommand(source, "gamerule doMobLoot false");
        commands.handleCommand(source, "gamerule doMobSpawning false");
        commands.handleCommand(source, "difficulty peaceful");
        commands.handleCommand(source, "kill @e[type=!minecraft:player]");
        commands.handleCommand(source, "time set day");
        commands.handleCommand(source, "weather clear");
        commands.handleCommand(source, "gamerule doMobLoot true");
        return 1;
    }
}

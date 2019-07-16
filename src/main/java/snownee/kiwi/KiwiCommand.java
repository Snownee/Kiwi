package snownee.kiwi;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.kiwi.util.LootDumper;

public class KiwiCommand
{
    private static final SimpleCommandExceptionType WRONG_PATTERN_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.kiwi.dumpLoots.wrongPattern"));

    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        /* off */
        dispatcher.register(Commands.literal(Kiwi.MODID)
                .then(Commands.literal("dumpLoots")
                        .executes(ctx -> dumpLoots(ctx.getSource(), ".+"))
                        .then(Commands.argument("pattern", StringArgumentType.string())
                                .executes(ctx -> dumpLoots(ctx.getSource(), StringArgumentType.getString(ctx, "pattern"))))));
        /* on */
    }

    public static int dumpLoots(CommandSource source, String pattern) throws CommandSyntaxException
    {
        try
        {
            Pattern p = Pattern.compile(pattern);
            int r = LootDumper.dump(p.asPredicate(), source.getServer().getDataDirectory());
            if (r == 0)
            {
                source.sendErrorMessage(new TranslationTextComponent("commands.kiwi.dumpLoots.noTargets"));
            }
            else
            {
                source.sendFeedback(new TranslationTextComponent("commands.kiwi.dumpLoots.success", r), true);
            }
            return r;
        }
        catch (PatternSyntaxException e)
        {
            throw WRONG_PATTERN_EXCEPTION.create();
        }
    }
}

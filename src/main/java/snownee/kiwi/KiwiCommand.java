package snownee.kiwi;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
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
                .argument("gamemode", IntegerArgumentType.integer(0, GameType.values().length - 1))
                .requires(ctx -> ctx.hasPermissionLevel(2))
                .executes(ctx -> setGameMode(ctx, Collections.singleton(ctx.getSource().asPlayer()), IntegerArgumentType.getInteger(ctx, "gamemode")))
                .then(Commands
                        .argument("target", EntityArgument.players())
                        .executes(ctx -> setGameMode(ctx, EntityArgument.getPlayers(ctx, "target"), IntegerArgumentType.getInteger(ctx, "gamemode")))
                )
        );

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

    private static int setGameMode(CommandContext<CommandSource> source, Collection<ServerPlayerEntity> players, int gameType) {
        GameType gameTypeIn = GameType.getByID(gameType);
        int i = 0;
        for (ServerPlayerEntity serverplayerentity : players) {
            if (serverplayerentity.interactionManager.getGameType() != gameTypeIn) {
                serverplayerentity.setGameType(gameTypeIn);
                sendGameModeFeedback(source.getSource(), serverplayerentity, gameTypeIn);
                ++i;
            }
        }
        return i;
    }

    private static void sendGameModeFeedback(CommandSource source, ServerPlayerEntity player, GameType gameTypeIn) {
        ITextComponent itextcomponent = new TranslationTextComponent("gameMode." + gameTypeIn.getName());
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslationTextComponent("commands.gamemode.success.self", itextcomponent), true);
        } else {
            if (source.getWorld().getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK)) {
                player.sendMessage(new TranslationTextComponent("gameMode.changed", itextcomponent));
            }
            source.sendFeedback(new TranslationTextComponent("commands.gamemode.success.other", player.getDisplayName(), itextcomponent), true);
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

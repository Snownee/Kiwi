package snownee.kiwi.command;

import java.util.List;
import java.util.Objects;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import snownee.kiwi.Kiwi;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.KEval;

public class KiwiCommand {

	public static void register(
			CommandDispatcher<CommandSourceStack> dispatcher,
			CommandBuildContext registryAccess,
			Commands.CommandSelection environment) {
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
				.then(Commands.argument("configFile", StringArgumentType.greedyString())
						.executes(ctx -> {
							String fileName = StringArgumentType.getString(ctx, "configFile");
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

		builder.then(Commands
				.literal("eval")
				.requires(ctx -> ctx.hasPermission(2))
				.then(Commands.argument("expression", StringArgumentType.greedyString())
						.executes(ctx -> eval(ctx.getSource(), StringArgumentType.getString(ctx, "expression")))
				)
		);
		/* on */
		dispatcher.register(builder);
	}

	private static int cleanLevel(CommandSourceStack commandSourceStack) {
		Commands commands = commandSourceStack.getServer().getCommands();
		List<String> rules = List.of(
				"gamerule doDaylightCycle false",
				"gamerule doWeatherCycle false",
				"gamerule doMobLoot false",
				"gamerule doMobSpawning false",
				"gamerule keepInventory true",
				"gamerule doTraderSpawning false",
				"gamerule doInsomnia false",
				"difficulty peaceful",
				"kill @e[type=!minecraft:player]",
				"time set day",
				"weather clear",
				"gamerule doMobLoot true"
		);
		for (String rule : rules) {
			commands.performPrefixedCommand(commandSourceStack, rule);
		}
		return 1;
	}

	private static int eval(CommandSourceStack source, String expString) {
		try {
			EvaluationValue value = new Expression(expString, KEval.config()).evaluate();
			String s;
			if (value.isNumberValue()) {
				s = value.getNumberValue().toPlainString();
			} else {
				s = Objects.toString(value.getValue());
			}
			source.sendSuccess(() -> Component.literal(s), false);
			return value.isNullValue() ? 0 : value.getNumberValue().intValue();
		} catch (Throwable e) {
			if (!Platform.isProduction()) {
				Kiwi.LOGGER.error(expString, e);
			}
			source.sendFailure(Component.literal(e.getLocalizedMessage()));
			return 0;
		}
	}
}

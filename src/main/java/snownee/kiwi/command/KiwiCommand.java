package snownee.kiwi.command;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiCommonConfig;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.KEval;
import snownee.kiwi.util.KUtil;

public class KiwiCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(Kiwi.ID);
		/* off */
		builder.then(Commands
				.literal("dev_env_rules")
				.then(Commands.literal("do_not_run_this_if_you_do_not_know_what_it_does")
						.requires(ctx -> ctx.hasPermission(2))
						.executes(ctx -> debugRules(ctx.getSource()))));

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

		builder.then(Commands
				.literal("eval")
				.requires(ctx -> ctx.hasPermission(2))
				.executes(ctx -> evalHelp(ctx.getSource(), CommandSourceStack::sendFailure))
				.then(Commands.argument("expression", StringArgumentType.greedyString())
						.executes(ctx -> eval(
								StringArgumentType.getString(ctx, "expression"),
								KiwiCommonConfig.evalPrintExpression,
								ctx.getSource(),
								(source, msg) -> source.sendSuccess(() -> msg, false),
								CommandSourceStack::sendFailure)
						)
				)
		);
		/* on */
		dispatcher.register(builder);
	}

	private static int debugRules(CommandSourceStack commandSourceStack) {
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

	public static <T> int evalHelp(T ctx, BiConsumer<T, Component> send) {
		String url = "https://github.com/Snownee/Kiwi/wiki/Eval-Guide";
		send.accept(ctx,
				Component.literal(url)
						.withStyle(s -> s.withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))));
		return 0;
	}

	public static <T> int eval(
			String expString,
			boolean print,
			T source,
			BiConsumer<T, Component> sendSuccess,
			BiConsumer<T, Component> sendFailure) {
		try {
			if (print) {
				sendSuccess.accept(source, Component.literal(">>> " + expString).withStyle(ChatFormatting.GREEN));
			}
			EvaluationValue value = new Expression(expString, KEval.config()).evaluate();
			String s;
			if (value.isNumberValue()) {
				s = new DecimalFormat("###,###.#####").format(value.getNumberValue());
			} else if (value.isExpressionNode() || value.isBinaryValue()) {
				s = "[%s]".formatted(value.getDataType());
			} else {
				s = Objects.toString(value.getValue());
			}
			sendSuccess.accept(source, KUtil.clickToCopy(Component.literal(s)));
			return value.isNullValue() ? 0 : value.getNumberValue().intValue();
		} catch (Throwable e) {
			if (!Platform.isProduction()) {
				Kiwi.LOGGER.error(expString, e);
			}
			sendFailure.accept(source, Component.literal("%s - %s".formatted(e, e.getLocalizedMessage())));
			return 0;
		}
	}
}

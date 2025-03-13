package snownee.kiwi.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class KalcCommand {

	public static <T> LiteralArgumentBuilder<T> create(ClientCommandContext<T> context) {
//		SuggestionProviders.register()
		return context.literal("kalc").executes(ctx -> KiwiCommand.evalHelp(ctx.getSource(), context::sendFailure)).then(context.argument(
								"expression",
								StringArgumentType.greedyString())
						.executes(ctx -> KiwiCommand.eval(
								StringArgumentType.getString(ctx, "expression"),
								true,
								ctx.getSource(),
								context::sendSuccess, context::sendFailure))
//				.suggests((context1, builder) -> {
//					Tokenizer tokenizer = new Tokenizer(builder.getRemaining(), KEval.config());
//					try {
//						tokenizer.parse();
//					} catch (ParseException e) {
//						Kiwi.LOGGER.info("{} {}", e, e.getStartPosition());
//					}
//
//					return builder.createOffset(builder.getStart() + 2).suggest("test").suggest("test2").buildFuture();
//				})
		);
	}

}

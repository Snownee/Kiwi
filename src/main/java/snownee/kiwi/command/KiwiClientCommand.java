package snownee.kiwi.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.Minecraft;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiCommonConfig;

public class KiwiClientCommand {

	public static <T> LiteralArgumentBuilder<T> create(ClientCommandContext<T> context) {
		LiteralArgumentBuilder<T> builder = context.literal(Kiwi.ID + "c");
		builder.then(context.literal("eval")
				.executes(ctx -> KiwiCommand.evalHelp(ctx.getSource(), context::sendFailure))
				.then(context.argument("expression", StringArgumentType.greedyString()).executes(ctx -> KiwiCommand.eval(
						StringArgumentType.getString(ctx, "expression"),
						KiwiCommonConfig.evalPrintExpression,
						ctx.getSource(),
						context::sendSuccess,
						context::sendFailure))));
		builder.then(context.literal("quiet")
				.executes(ctx -> quiet()));
		return builder;
	}

	private static int quiet() {
		Minecraft mc = Minecraft.getInstance();
		mc.gui.getChat().trimmedMessages.clear();
		mc.gui.clearTitles();
		mc.getToastManager().clear();
		mc.getSoundManager().stop();
		return 0;
	}

}

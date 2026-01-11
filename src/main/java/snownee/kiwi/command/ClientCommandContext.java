package snownee.kiwi.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public record ClientCommandContext<S>(CommandBuildContext buildContext) {

	public LiteralArgumentBuilder<S> literal(String s) {
		//noinspection unchecked
		return (LiteralArgumentBuilder<S>) ClientCommandManager.literal(s);
	}

	public <T> RequiredArgumentBuilder<S, T> argument(String s, ArgumentType<T> type) {
		//noinspection unchecked
		return (RequiredArgumentBuilder<S, T>) ClientCommandManager.argument(s, type);
	}

	public void sendSuccess(S source, Component message) {
		((FabricClientCommandSource) source).sendFeedback(message);
	}

	public void sendFailure(S source, Component message) {
		((FabricClientCommandSource) source).sendError(message);
	}
}

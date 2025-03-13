package snownee.kiwi.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public record ClientCommandContext<S>(CommandBuildContext buildContext) {

	public LiteralArgumentBuilder<S> literal(String s) {
		//noinspection unchecked
		return (LiteralArgumentBuilder<S>) Commands.literal(s);
	}

	public <T> RequiredArgumentBuilder<S, T> argument(String s, ArgumentType<T> type) {
		//noinspection unchecked
		return (RequiredArgumentBuilder<S, T>) Commands.argument(s, type);
	}

	public void sendSuccess(S source, Component message) {
		((CommandSourceStack) source).sendSuccess(() -> message, true);
	}

	public void sendFailure(S source, Component message) {
		((CommandSourceStack) source).sendFailure(message);
	}
}

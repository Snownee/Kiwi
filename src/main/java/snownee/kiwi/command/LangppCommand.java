package snownee.kiwi.command;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class LangppCommand {
	public static final List<String> KEYS = List.of(
			"langpp.test.viewer",
			"langpp.test.kiwi",
			"langpp.test.nested",
			"langpp.test.escape",
			"langpp.test.version",
			"langpp.test.cfg");

	private LangppCommand() {
	}

	public static <T> int print(ClientCommandContext<T> context, T source) {
		for (String key : KEYS) {
			context.sendSuccess(source, Component.literal(key + " = ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal(I18n.get(key))));
		}
		return KEYS.size();
	}
}

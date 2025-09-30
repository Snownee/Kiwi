package snownee.kiwi.customization.command;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import snownee.kiwi.Kiwi;

public class ExportMappingsCommand {

	public static void register(LiteralArgumentBuilder<CommandSourceStack> builder) {
		builder.then(Commands
				.literal("mappings")
				.executes(ctx -> export(ctx.getSource())));
	}

	private static int export(CommandSourceStack source) {
		Map<String, String> data = Maps.newTreeMap();
		scanRegistry(BuiltInRegistries.BLOCK, data);
		scanRegistry(BuiltInRegistries.ITEM, data);
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("mapping.kiwi"))) {
			new GsonBuilder().setPrettyPrinting().create().toJson(data, writer);
		} catch (Exception e) {
			Kiwi.LOGGER.error("Failed to export mappings", e);
			source.sendFailure(Component.literal("Failed to export mappings: " + e.getMessage()));
			return 0;
		}
		source.sendSuccess(() -> Component.literal("Done."), false);
		return 1;
	}

	private static void scanRegistry(Registry<?> registry, Map<String, String> data) {
		for (Object o : registry) {
			String className = o.getClass().getName();
			if (!className.startsWith("net.minecraft.")) {
				continue;
			}
			String mapped = className.substring(14);
			if (data.containsKey(mapped)) {
				continue;
			}
			String unmapped = FabricLoader.getInstance().getMappingResolver().unmapClassName("intermediary", className);
			unmapped = unmapped.substring(14);
			data.put(mapped, unmapped);
		}
	}
}

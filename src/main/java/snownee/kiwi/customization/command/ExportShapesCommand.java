package snownee.kiwi.customization.command;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import snownee.kiwi.customization.CustomizationHooks;
import snownee.kiwi.customization.block.BlockFundamentals;
import snownee.kiwi.customization.shape.ShapeGenerator;
import snownee.kiwi.customization.shape.UnbakedShapeCodec;

public class ExportShapesCommand {
	public static void register(LiteralArgumentBuilder<CommandSourceStack> builder) {
		builder.then(Commands
				.literal("shapes")
				.executes(ctx -> exportShapes(ctx.getSource())));
	}

	private static int exportShapes(CommandSourceStack source) {
		Map<String, String> data = Maps.newTreeMap();
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("exported_shapes.json"))) {
			BlockFundamentals fundamentals = BlockFundamentals.reload(CustomizationHooks.collectKiwiPacks(), false);
			fundamentals.shapes().forEach((key, value) -> {
				if (value.getClass() != ShapeGenerator.Unit.class) {
					return;
				}
				String string = key.toString();
				if ("minecraft:empty".equals(string) || "minecraft:block".equals(string)) {
					return;
				}
				data.put(string, UnbakedShapeCodec.encodeVoxelShape(ShapeGenerator.Unit.unboxOrThrow(value)));
			});
			new GsonBuilder().setPrettyPrinting().create().toJson(data, writer);
		} catch (Exception e) {
			source.sendFailure(Component.literal(e.getMessage()));
			return 0;
		}
		source.sendSuccess(() -> Component.literal("Shapes exported"), false);
		return 1;
	}
}

package snownee.kiwi.customization.command;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.CustomizationHooks;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.builder.BuilderRules;

public class ReloadFamiliesAndRulesCommand {

	public static void register(LiteralArgumentBuilder<CommandSourceStack> builder) {
		builder.then(Commands
				.literal("families_and_rules")
				.executes(ctx -> reload(ctx.getSource())));
	}

	private static int reload(CommandSourceStack source) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		ResourceManager resourceManager = CustomizationHooks.collectKiwiPacks();
		int familyCount = BlockFamilies.reload(resourceManager);
		int ruleCount = BuilderRules.reload(resourceManager);
		long reloadTime = stopwatch.elapsed().toMillis();
		Kiwi.LOGGER.info("Reload time: %dms".formatted(reloadTime));
		source.sendSuccess(() -> Component.literal("%d block families have been reloaded".formatted(familyCount)), false);
		source.sendSuccess(() -> Component.literal("%d builder rules have been reloaded".formatted(ruleCount)), false);
		return 1;
	}
}

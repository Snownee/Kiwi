package snownee.kiwi.customization.command;

import java.util.Set;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.CustomizationHooks;
import snownee.kiwi.customization.block.BlockFundamentals;
import snownee.kiwi.customization.block.KBlockSettings;
import snownee.kiwi.customization.block.loader.KBlockDefinition;

public class ReloadBlockSettingsCommand {

	public static void register(LiteralArgumentBuilder<CommandSourceStack> builder) {
		builder.then(Commands
				.literal("block_settings")
				.executes(ctx -> reload(ctx.getSource())));
	}

	private static int reload(CommandSourceStack source) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		BlockFundamentals fundamentals = BlockFundamentals.reload(CustomizationHooks.collectKiwiPacks(), false);
		long parseTime = stopwatch.elapsed().toMillis();
		stopwatch.reset().start();
		Set<Block> set = Sets.newHashSet();
		BuiltInRegistries.BLOCK.holders().forEach(holder -> {
			KBlockDefinition definition = fundamentals.blocks().get(holder.key().location());
			if (definition == null || !set.add(holder.value())) {
				return;
			}
			KBlockSettings.Builder builder = definition.createSettings(holder.key().location(), fundamentals.shapes());
			holder.value().properties = builder.get();
			KBlockDefinition.setConfiguringShape(holder.value());
		});
		Blocks.rebuildCache();
		ReloadSlotsCommand.reload(fundamentals);
		long attachTime = stopwatch.elapsed().toMillis();
		Kiwi.LOGGER.info("Parse time %dms + Attach time %dms = %dms".formatted(parseTime, attachTime, parseTime + attachTime));
		source.sendSuccess(() -> Component.literal("%d Block settings reloaded".formatted(set.size())), false);
		return 1;
	}

}

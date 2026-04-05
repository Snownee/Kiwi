package snownee.kiwi.customization.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.block.family.BlockFamily;
import snownee.kiwi.util.KHolder;

public class PrintFamiliesCommand {

	public static void register(LiteralArgumentBuilder<CommandSourceStack> builder) {
		builder.then(Commands
				.literal("print_families")
				.executes(ctx -> print(ctx.getSource())));
	}

	private static int print(CommandSourceStack source) {
		for (KHolder<BlockFamily> family : BlockFamilies.all()) {
			Kiwi.LOGGER.info(family.key() + ":");
			for (Holder.Reference<Block> holder : family.value().blockHolders()) {
				Kiwi.LOGGER.info("  - " + holder.unwrapKey().orElseThrow().identifier());
			}
		}
		source.sendSuccess(() -> Component.literal("Done."), false);
		return 1;
	}
}

package snownee.kiwi.customization.command;

import java.util.Collection;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.block.family.BlockFamily;
import snownee.kiwi.customization.block.family.BlockFamilyInferrer;
import snownee.kiwi.util.KHolder;

public class PrintFamiliesCommand {

	public static void register(LiteralArgumentBuilder<CommandSourceStack> builder) {
		builder.then(Commands
				.literal("print_families")
				.executes(ctx -> print(ctx.getSource())));
	}

	private static int print(CommandSourceStack source) {
		Collection<KHolder<BlockFamily>> families = new BlockFamilyInferrer().generate();
		for (KHolder<BlockFamily> family : families) {
			Kiwi.LOGGER.info(family.key().toString() + ":");
			for (Holder.Reference<Block> holder : family.value().blockHolders()) {
				Kiwi.LOGGER.info("  - " + holder.unwrapKey().orElseThrow().location());
			}
		}
		source.sendSuccess(() -> Component.literal("Done."), false);
		return 1;
	}
}

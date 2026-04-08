package snownee.kiwi.mixin.forge;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.Block;

@Mixin(BlockColors.class)
public interface BlockColorsAccess {
	@Accessor("sources")
	Map<Block, List<BlockTintSource>> getBlockColors();
}
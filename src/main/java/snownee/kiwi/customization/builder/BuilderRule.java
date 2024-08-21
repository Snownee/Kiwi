package snownee.kiwi.customization.builder;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface BuilderRule {
	Codec<BuilderRule> CODEC = Codec.unit(null);

	Stream<Block> relatedBlocks();

	boolean matches(Player player, ItemStack itemStack, BlockState blockState);

	void apply(UseOnContext context, List<BlockPos> positions);

	List<BlockPos> searchPositions(UseOnContext context);
}

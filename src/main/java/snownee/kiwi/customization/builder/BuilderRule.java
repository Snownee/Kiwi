package snownee.kiwi.customization.builder;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.customization.CustomizationRegistries;

public interface BuilderRule {
	Codec<BuilderRule> CODEC = ExtraCodecs.lazyInitializedCodec(() -> CustomizationRegistries.BUILDER_RULE.byNameCodec()
			.dispatch(BuilderRule::type, BuilderRule.Type::codec));

	Type<?> type();

	Stream<Block> relatedBlocks();

	boolean matches(Player player, ItemStack itemStack, BlockState blockState);

	void apply(UseOnContext context, List<BlockPos> positions);

	List<BlockPos> searchPositions(BlockState blockState, UseOnContext context);

	default void playPlaceSound(Player player, BlockState blockState) {
		SoundType soundType = blockState.getSoundType();
		player.level().playSound(
				null,
				player.blockPosition(),
				soundType.getPlaceSound(),
				SoundSource.BLOCKS,
				(soundType.getVolume() + 1.0F) / 2.0F,
				soundType.getPitch() * 0.8F);
	}

	record Type<T extends BuilderRule>(Codec<T> codec) {}
}
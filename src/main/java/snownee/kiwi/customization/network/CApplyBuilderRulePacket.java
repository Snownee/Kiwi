package snownee.kiwi.customization.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.minecraft.util.ByIdMap;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import snownee.kiwi.Kiwi;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;
import snownee.kiwi.customization.builder.BuilderRule;
import snownee.kiwi.customization.builder.BuilderRules;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.util.KHolder;

@KiwiPacket
public record CApplyBuilderRulePacket(
		InteractionHand hand,
		BlockPos clickPos,
		ResourceLocation key,
		List<BlockPos> positions) implements CustomPacketPayload {

	public CApplyBuilderRulePacket(UseOnContext context, KHolder<BuilderRule> holder, List<BlockPos> positions) {
		this(context.getHand(), context.getClickedPos(), holder.key(), positions);
	}

	public static final Type<CApplyBuilderRulePacket> TYPE = new Type<>(Kiwi.id("apply_builder_rule"));

	@Override
	public @NotNull Type<CApplyBuilderRulePacket> type() {
		return TYPE;
	}
	public static class Handler implements PlayPacketHandler<CApplyBuilderRulePacket> {

		public static final IntFunction<InteractionHand> HAND_ID_MAPPER = ByIdMap.continuous(InteractionHand::ordinal, InteractionHand.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
		public static final StreamCodec<ByteBuf, InteractionHand> HAND_STREAM_CODEC = ByteBufCodecs.idMapper(HAND_ID_MAPPER, InteractionHand::ordinal);

		public static final StreamCodec<RegistryFriendlyByteBuf, CApplyBuilderRulePacket> STREAM_CODEC = StreamCodec.composite(
				HAND_STREAM_CODEC, CApplyBuilderRulePacket::hand,
				BlockPos.STREAM_CODEC, CApplyBuilderRulePacket::clickPos,
				ResourceLocation.STREAM_CODEC, CApplyBuilderRulePacket::key,
				ByteBufCodecs.collection(ArrayList::new, BlockPos.STREAM_CODEC), CApplyBuilderRulePacket::positions,
				CApplyBuilderRulePacket::new
		);

		@Override
		public void handle(CApplyBuilderRulePacket packet, PayloadContext context) {
			var player = context.serverPlayer();
			InteractionHand hand = packet.hand;
			BlockPos pos = packet.clickPos;
			ResourceLocation ruleId = packet.key;
			List<BlockPos> positions = packet.positions;
			if (Stream.concat(Stream.of(pos), positions.stream()).anyMatch($ -> !player.level().isLoaded($))) {
				return;
			}
			context.execute(() -> {
				BuilderRule rule = BuilderRules.get(ruleId);
				if (rule == null) {
					return;
				}
				HitResult hitResult = player.pick(10, 1, false);
				if (!(hitResult instanceof BlockHitResult blockHitResult) || !blockHitResult.getBlockPos().equals(pos)) {
					return;
				}
				BlockState blockState = player.level().getBlockState(pos);
				Block block = blockState.getBlock();
				if (rule.relatedBlocks().noneMatch(block::equals)) {
					return;
				}
				UseOnContext useOnContext = new UseOnContext(player, hand, blockHitResult);
				if (rule.matches(player, useOnContext.getItemInHand(), blockState)) {
					rule.apply(useOnContext, positions);
				}
			});
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CApplyBuilderRulePacket> streamCodec() {
			return STREAM_CODEC;
		}
	}

}

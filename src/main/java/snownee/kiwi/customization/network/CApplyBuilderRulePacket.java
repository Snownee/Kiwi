package snownee.kiwi.customization.network;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import snownee.kiwi.util.KHolder;
import snownee.kiwi.customization.builder.BuilderRule;
import snownee.kiwi.customization.builder.BuilderRules;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket(value = "apply_builder_rule", dir = KiwiPacket.Direction.PLAY_TO_SERVER)
public class CApplyBuilderRulePacket extends PacketHandler {
	public static CApplyBuilderRulePacket I;

	public static void send(UseOnContext context, KHolder<BuilderRule> holder, List<BlockPos> positions) {
		I.sendToServer(buf -> {
			buf.writeEnum(context.getHand());
			buf.writeBlockPos(context.getClickedPos());
			buf.writeResourceLocation(holder.key());
			buf.writeCollection(positions, FriendlyByteBuf::writeBlockPos);
		});
	}

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(
			Function<Runnable, CompletableFuture<FriendlyByteBuf>> function,
			FriendlyByteBuf buf,
			@Nullable ServerPlayer player) {
		Objects.requireNonNull(player);
		InteractionHand hand = buf.readEnum(InteractionHand.class);
		BlockPos pos = buf.readBlockPos();
		ResourceLocation ruleId = buf.readResourceLocation();
		List<BlockPos> positions = buf.readCollection(Lists::newArrayListWithExpectedSize, FriendlyByteBuf::readBlockPos);
		if (Stream.concat(Stream.of(pos), positions.stream()).anyMatch($ -> !player.level().isLoaded($))) {
			return null;
		}
		return function.apply(() -> {
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
			UseOnContext context = new UseOnContext(player, hand, blockHitResult);
			if (rule.matches(player, context.getItemInHand(), blockState)) {
				rule.apply(context, positions);
			}
		});
	}
}

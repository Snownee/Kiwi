package snownee.kiwi.contributor.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;
import snownee.kiwi.util.KUtil;

@KiwiPacket
public record CSetCosmeticPacket(@Nullable ResourceLocation id) implements CustomPacketPayload {
	public static final Type<CSetCosmeticPacket> TYPE = CustomPacketPayload.createType("kiwi:set_cosmetic");

	@Override
	public @NotNull Type<CSetCosmeticPacket> type() {
		return TYPE;
	}

	public static class Handler implements PlayPacketHandler<CSetCosmeticPacket> {
		public static final StreamCodec<RegistryFriendlyByteBuf, CSetCosmeticPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8.map(it -> it.isEmpty() ? null : KUtil.RL(it), it -> it == null ? "" : it.toString()),
				CSetCosmeticPacket::id,
				CSetCosmeticPacket::new
		);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CSetCosmeticPacket> streamCodec() {
			return STREAM_CODEC;
		}

		@Override
		public void handle(CSetCosmeticPacket packet, PayloadContext context) {
			context.execute(() -> Contributors.changeCosmetic(context.serverPlayer(), packet.id));
		}
	}
}

package snownee.kiwi.test;

import java.util.Objects;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import snownee.kiwi.Kiwi;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;

@KiwiPacket
public record SMyPacket(int number) implements CustomPacketPayload {
	public static final Type<CustomPacketPayload> TYPE = new CustomPacketPayload.Type<>(Kiwi.id("my"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static class Handler implements PlayPacketHandler<SMyPacket> {
		public static final StreamCodec<RegistryFriendlyByteBuf, SMyPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT,
				SMyPacket::number,
				SMyPacket::new
		);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SMyPacket> streamCodec() {
			return STREAM_CODEC;
		}

		@Override
		public void handle(SMyPacket packet, PayloadContext context) {
			context.execute(() -> Kiwi.LOGGER.info(Objects.toString(packet.number())));
		}
	}
}

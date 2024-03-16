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
public record CMyPacket(int number) implements CustomPacketPayload {
	public static final Type<CustomPacketPayload> TYPE = CustomPacketPayload.createType("kiwi:my");

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static class Handler implements PlayPacketHandler<CMyPacket> {
		public static final StreamCodec<RegistryFriendlyByteBuf, CMyPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT,
				CMyPacket::number,
				CMyPacket::new
		);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CMyPacket> streamCodec() {
			return STREAM_CODEC;
		}

		@Override
		public void handle(CMyPacket packet, PayloadContext context) {
			context.execute(() -> Kiwi.LOGGER.info(Objects.toString(packet.number())));
		}
	}
}

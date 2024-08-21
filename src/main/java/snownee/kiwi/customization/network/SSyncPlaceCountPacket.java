package snownee.kiwi.customization.network;

import java.util.Objects;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.duck.KPlayer;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;

@KiwiPacket
public record SSyncPlaceCountPacket(int placeCount) implements CustomPacketPayload {

	public static final Type<SSyncPlaceCountPacket> TYPE = new Type<>(Kiwi.id("sync_place_count"));

	@Override
	public Type<SSyncPlaceCountPacket> type() {
		return TYPE;
	}

	public static final class Handler implements PlayPacketHandler<SSyncPlaceCountPacket> {

		public static final StreamCodec<RegistryFriendlyByteBuf, SSyncPlaceCountPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, SSyncPlaceCountPacket::placeCount,
				SSyncPlaceCountPacket::new
		);

		@Override
		public void handle(SSyncPlaceCountPacket packet, PayloadContext context) {
			context.execute(() -> (Objects.requireNonNull((KPlayer) Minecraft.getInstance().player)).kiwi$setPlaceCount(packet.placeCount));
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SSyncPlaceCountPacket> streamCodec() {
			return STREAM_CODEC;
		}
	}

	public static void sync(ServerPlayer player) {
		KPacketSender.send(new SSyncPlaceCountPacket(((KPlayer) player).kiwi$getPlaceCount()), player);
	}

}

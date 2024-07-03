package snownee.kiwi.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface PlayPacketHandler<T extends CustomPacketPayload> {

	void handle(T packet, PayloadContext context);

	StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();

	default void register(KNetworking networking, CustomPacketPayload.Type<?> type, KiwiPacket.Direction direction) {
		//noinspection unchecked
		networking.registerPlayHandler((CustomPacketPayload.Type<T>) type, this, direction);
	}
}

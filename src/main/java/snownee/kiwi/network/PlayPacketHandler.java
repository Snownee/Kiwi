package snownee.kiwi.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public abstract class PlayPacketHandler<T extends CustomPacketPayload> {
	public abstract T read(RegistryFriendlyByteBuf buf);

	public abstract void write(T packet, RegistryFriendlyByteBuf buf);

	public abstract void handle(T packet, PayloadContext context);

	public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
		return CustomPacketPayload.codec(this::write, this::read);
	}

	public void register(CustomPacketPayload.Type<?> type, KiwiPacket.Direction direction) {
		//noinspection unchecked
		KNetworking.registerPlayHandler((CustomPacketPayload.Type<T>) type, this, direction);
	}
}

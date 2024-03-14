package snownee.kiwi.network;

import java.util.Objects;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface ServerPayloadContext extends PayloadContext {
	@Override
	default void execute(Runnable runnable) {
		Objects.requireNonNull(serverPlayer().getServer()).execute(runnable);
	}

	@Override
	default void sendPacket(CustomPacketPayload payload) {
		serverPlayer().connection.send(new ClientboundCustomPayloadPacket(payload));
	}

	@Override
	ServerPlayer serverPlayer();
}

package snownee.kiwi.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface PayloadContext {
	void execute(Runnable runnable);

	default void sendPacket(CustomPacketPayload payload) {
		throw new UnsupportedOperationException();
	}

	default ServerPlayer serverPlayer() {
		throw new UnsupportedOperationException();
	}
}

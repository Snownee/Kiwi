package snownee.kiwi.network;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.kiwi.network.KiwiPacket.Direction;

@FunctionalInterface
public interface IPacketHandler {
	CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, @Nullable ServerPlayer sender);

	default Direction getDirection() {
		return null;
	}
}

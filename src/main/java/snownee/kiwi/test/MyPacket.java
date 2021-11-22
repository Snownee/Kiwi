package snownee.kiwi.test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket("my")
public class MyPacket extends PacketHandler.Impl {
	public static MyPacket I;

	@Override
	public CompletableFuture<@Nullable FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<@Nullable FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer responseSender) {
		int number = buf.readVarInt();
		return executor.apply(() -> System.out.println(number));
	}

	public static void send(ServerPlayer player, int n) {
		I.send(player, $ -> $.writeVarInt(n));
	}

}

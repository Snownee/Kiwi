package snownee.kiwi.test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.kiwi.network.PacketHandler;
import snownee.kiwi.network.KiwiPacket;

@KiwiPacket("my")
public class MyPacket extends PacketHandler {
	public static MyPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer responseSender) {
		int number = buf.readVarInt();
		return executor.apply(() -> System.out.println(number));
	}

	public static void send(ServerPlayer player, int n) {
		I.send(player, $ -> $.writeVarInt(n));
	}

}

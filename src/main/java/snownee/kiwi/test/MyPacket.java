package snownee.kiwi.test;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import snownee.kiwi.network.PacketHandler;

// 设置包的类型名及发送方向。并自动注册
//@KiwiPacket(value = "my", dir = Direction.PLAY_TO_CLIENT)
public class MyPacket extends PacketHandler {
	// 将自动注册的实例注入到 I 中
	public static MyPacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor, FriendlyByteBuf buf, ServerPlayer responseSender) {
		int number = buf.readVarInt();
		// 在主线程上执行游戏相关行为
		return executor.apply(() -> System.out.println(number));
		// return CompletableFuture.completedFuture(null);
	}

	// 助手方法
	public static void send(ServerPlayer player, int n) {
		I.send(player, $ -> $.writeVarInt(n));
	}
}

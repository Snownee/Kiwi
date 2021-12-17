package snownee.kiwi.network;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.network.KiwiPacket.Direction;

public final class Networking {
	private Networking() {
	}

	public static synchronized void registerHandler(ResourceLocation id, IPacketHandler handler) {
		Direction direction = handler.getDirection();
		if (direction == Direction.PLAY_TO_CLIENT) {
			ClientPlayNetworking.PlayChannelHandler h = (client, listener, buf, responseSender) -> {
				handler.receive($ -> {
					CompletableFuture<FriendlyByteBuf> future = new CompletableFuture<>();
					Runnable runnable = () -> {
						$.run();
						future.complete(null);
					};
					client.execute(runnable);
					return future;
				}, buf, null);
			};
			ClientPlayNetworking.registerGlobalReceiver(id, h);
		} else if (direction == Direction.PLAY_TO_SERVER) {
			ServerPlayNetworking.PlayChannelHandler h = (server, player, listener, buf, responseSender) -> {
				handler.receive($ -> {
					CompletableFuture<FriendlyByteBuf> future = new CompletableFuture<>();
					Runnable runnable = () -> {
						$.run();
						future.complete(null);
					};
					server.execute(runnable);
					return future;
				}, buf, player);
			};
			ServerPlayNetworking.registerGlobalReceiver(id, h);
		}
	}

	public static void processClass(String className, String modId) {
		try {
			Class<? extends PacketHandler> clazz = (Class<? extends PacketHandler>) Class.forName(className);
			PacketHandler handler = clazz.getDeclaredConstructor().newInstance();
			handler.setModId(modId);
			registerHandler(handler.id, handler);
			Field field = clazz.getDeclaredField("I");
			field.set(null, handler);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}

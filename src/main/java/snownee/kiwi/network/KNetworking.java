package snownee.kiwi.network;

import java.lang.reflect.Field;

import com.google.common.base.Preconditions;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import snownee.kiwi.KiwiAnnotationData;
import snownee.kiwi.loader.Platform;

public final class KNetworking {
	private KNetworking() {
	}

	public static synchronized <T extends CustomPacketPayload> void registerPlayHandler(
			CustomPacketPayload.Type<T> type,
			PlayPacketHandler<T> handler,
			KiwiPacket.Direction direction) {
		Preconditions.checkArgument(direction != KiwiPacket.Direction.AUTO, "Direction must be specified");
		if (direction == KiwiPacket.Direction.TO_CLIENT) {
			PayloadTypeRegistry.playS2C().register(type, handler.streamCodec());
			if (Platform.isPhysicalClient()) {
				ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
					handler.handle(payload, context.client()::execute);
				});
			}
		} else if (direction == KiwiPacket.Direction.TO_SERVER) {
			PayloadTypeRegistry.playC2S().register(type, handler.streamCodec());
			ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
				handler.handle(payload, (ServerPayloadContext) context::player);
			});
		}
	}

	public static void processClass(KiwiAnnotationData annotationData) {
		try {
			String packetClassName = annotationData.getTarget();
			if (Platform.isProduction() && packetClassName.startsWith("snownee.kiwi.test.")) {
				return;
			}
			Class<?> packetClass = Class.forName(packetClassName);
			Field field = packetClass.getDeclaredField("TYPE");
			CustomPacketPayload.Type<?> type = (CustomPacketPayload.Type<?>) field.get(null);
			KiwiPacket annotation = packetClass.getDeclaredAnnotation(KiwiPacket.class);
			KiwiPacket.Direction direction = annotation.dir();
			if (direction == KiwiPacket.Direction.AUTO) {
				String simpleName = packetClass.getSimpleName();
				char firstChar = simpleName.charAt(0);
				Preconditions.checkState(
						firstChar == 'C' || firstChar == 'S',
						"Packet class name must be '[CS][A-Z]', but got '%s'",
						simpleName);
				Preconditions.checkState(
						Character.isUpperCase(simpleName.charAt(1)),
						"Packet class name must be '[CS][A-Z]', but got '%s'",
						simpleName);
				if (firstChar == 'C') {
					direction = KiwiPacket.Direction.TO_SERVER;
				} else {
					direction = KiwiPacket.Direction.TO_CLIENT;
				}
			}
			Class<?> handlerClass = Class.forName(packetClassName + "$Handler");
			if (PlayPacketHandler.class.isAssignableFrom(handlerClass)) {
				PlayPacketHandler<?> handler = (PlayPacketHandler<?>) handlerClass.getDeclaredConstructor().newInstance();
				handler.register(type, direction);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}

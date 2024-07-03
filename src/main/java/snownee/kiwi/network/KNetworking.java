package snownee.kiwi.network;

import java.lang.reflect.Field;

import com.google.common.base.Preconditions;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import snownee.kiwi.KiwiAnnotationData;
import snownee.kiwi.loader.Platform;

public final class KNetworking {

	private final IEventBus modEventBus;

	public KNetworking(IEventBus modEventBus) {
		this.modEventBus = modEventBus;
	}

	public <T extends CustomPacketPayload> void registerPlayHandler(
			CustomPacketPayload.Type<T> type,
			PlayPacketHandler<T> handler,
			KiwiPacket.Direction direction) {
		Preconditions.checkArgument(direction != KiwiPacket.Direction.AUTO, "Direction must be specified");
		modEventBus.addListener((RegisterPayloadHandlersEvent event) -> {
			PayloadRegistrar registrar = event.registrar(type.id().toString()).optional();
			if (direction == KiwiPacket.Direction.TO_CLIENT) {
				registrar.playToClient(type, handler.streamCodec(), (payload, context) -> {
					handler.handle(payload, context::enqueueWork);
				});
			} else if (direction == KiwiPacket.Direction.TO_SERVER) {
				registrar.playToServer(type, handler.streamCodec(), (payload, context) -> {
					handler.handle(payload, (ServerPayloadContext) () -> (ServerPlayer) context.player());
				});
			}
		});
	}

	public void processClass(KiwiAnnotationData annotationData) {
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
				handler.register(this, type, direction);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}

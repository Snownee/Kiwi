package snownee.kiwi.network;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;

@Retention(RUNTIME)
@Target(TYPE)
public @interface KiwiPacket {

	String value();

	Direction dir() default Direction.PLAY_TO_SERVER;

	public enum Direction {
		PLAY_TO_SERVER(ClientNetworkingImpl.PLAY),
		PLAY_TO_CLIENT(ServerNetworkingImpl.PLAY),
		LOGIN_TO_SERVER(ClientNetworkingImpl.LOGIN),
		LOGIN_TO_CLIENT(ServerNetworkingImpl.LOGIN);

		final GlobalReceiverRegistry<?> value;

		Direction(GlobalReceiverRegistry<?> value) {
			this.value = value;
		}
	}
}

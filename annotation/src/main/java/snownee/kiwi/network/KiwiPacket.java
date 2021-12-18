package snownee.kiwi.network;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface KiwiPacket {

	String value();

	Direction dir() default Direction.PLAY_TO_SERVER;

	public enum Direction {
		PLAY_TO_SERVER, PLAY_TO_CLIENT, LOGIN_TO_SERVER, LOGIN_TO_CLIENT;
	}
}

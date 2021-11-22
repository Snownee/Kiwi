package snownee.kiwi.network;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface KiwiPacket {

	String value();

	Networking.Direction dir() default Networking.Direction.PLAY_TO_SERVER;

}

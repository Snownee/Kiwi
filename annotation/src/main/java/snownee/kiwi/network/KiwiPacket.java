package snownee.kiwi.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KiwiPacket {
	Direction dir() default Direction.AUTO;

	enum Direction {
		AUTO, TO_SERVER, TO_CLIENT, CUSTOM;
	}
}

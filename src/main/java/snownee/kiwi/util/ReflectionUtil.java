package snownee.kiwi.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class ReflectionUtil {
    private ReflectionUtil() {}

    public static void setFinalValue(Field field, Object obj, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        field.setAccessible(true);
        Field modifiers = field.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(obj, null);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }
}

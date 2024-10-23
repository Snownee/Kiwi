package snownee.kiwi.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.jetbrains.annotations.NotNull;

import javax.annotation.meta.TypeQualifierDefault;

@NotNull
@TypeQualifierDefault({ElementType.METHOD, ElementType.PARAMETER})
@Target({ElementType.TYPE, ElementType.PACKAGE})
public @interface NotNullByDefault {
}
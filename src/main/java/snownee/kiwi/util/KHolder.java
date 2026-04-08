package snownee.kiwi.util;

import net.minecraft.resources.Identifier;

// provide ID information to make us easier when debugging
public record KHolder<T>(Identifier key, T value) {
}
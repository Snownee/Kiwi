package snownee.kiwi.util;

import net.minecraft.resources.ResourceLocation;

// provide ID information to make us easier when debugging
public record KHolder<T>(ResourceLocation key, T value) {
}
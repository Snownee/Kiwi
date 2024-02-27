package snownee.kiwi.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import snownee.kiwi.loader.Platform;

public class EnumUtil {
	public static final Direction[] DIRECTIONS = Direction.values();

	public static final Direction[] HORIZONTAL_DIRECTIONS = Arrays.stream(DIRECTIONS).filter($ -> {
		return $.getAxis().isHorizontal();
	}).sorted(Comparator.comparingInt($ -> {
		return $.get2DDataValue();
	})).toArray($ -> {
		return new Direction[$];
	});

	@Environment(EnvType.CLIENT)
	public static Set<RenderType> BLOCK_RENDER_TYPES;

	static {
		if (Platform.isPhysicalClient()) {
			BLOCK_RENDER_TYPES = ImmutableSet.of(
					RenderType.solid(),
					RenderType.cutout(),
					RenderType.cutoutMipped(),
					RenderType.translucent());
		}
	}
}

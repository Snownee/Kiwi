package snownee.kiwi.util;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import snownee.kiwi.loader.Platform;

public class EnumUtil {
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

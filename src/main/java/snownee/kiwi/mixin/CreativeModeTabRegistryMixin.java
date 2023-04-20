package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.CreativeModeTabRegistry;
import snownee.kiwi.KiwiTabBuilder;

@Mixin(value = CreativeModeTabRegistry.class, remap = false)
public class CreativeModeTabRegistryMixin {

	@Shadow
	private static BiMap<ResourceLocation, CreativeModeTab> creativeModeTabs;
	@Shadow
	private static Multimap<ResourceLocation, ResourceLocation> edges;

	@Inject(at = @At(value = "INVOKE", target = "recalculateItemCreativeModeTabs()V"), method = "fireCollectionEvent")
	private static void kiwi$fireCollectionEvent(CallbackInfo ci) {
		for (KiwiTabBuilder builder : KiwiTabBuilder.BUILDERS) {
			creativeModeTabs.put(builder.id, builder.build());
			edges.put(new ResourceLocation("spawn_eggs"), builder.id);
		}
	}

}

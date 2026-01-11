
package snownee.kiwi.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;
import snownee.kiwi.customization.CustomizationServiceFinder;

public class MixinPlugin implements IMixinConfigPlugin {
	private boolean customization;
	private boolean persistentCreativeInventory;
	private boolean fastScrolling;
	private boolean lavaClearView;

	public static boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public void onLoad(String mixinPackage) {
		boolean devEnv = FabricLoader.getInstance().isDevelopmentEnvironment();
		customization = CustomizationServiceFinder.shouldEnable(FabricLoader.getInstance().getAllMods());
		persistentCreativeInventory = customization || isModLoaded("persistentcreativeinventory") || devEnv;
		fastScrolling = isModLoaded("fastscroll") || devEnv;
		lavaClearView = isModLoaded("lavaclearview") || devEnv;
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.startsWith("snownee.kiwi.mixin.customization.")) {
			return customization;
		}
		return switch (mixinClassName) {
			case "snownee.kiwi.mixin.client.CreativeModeInventoryScreenMixin" -> persistentCreativeInventory;
			case "snownee.kiwi.mixin.client.OptionInstanceMixin" -> fastScrolling;
			case "snownee.kiwi.mixin.client.FogRendererMixin", "snownee.kiwi.mixin.client.ScreenEffectRendererMixin" -> lavaClearView;
			default -> true;
		};
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}

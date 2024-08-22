package snownee.kiwi.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.LoadingModList;
import snownee.kiwi.customization.CustomizationServiceFinder;

public class MixinPlugin implements IMixinConfigPlugin {
	private boolean customization;
	private boolean persistentCreativeInventory;
	private boolean fastScrolling;
	private boolean lavaClearView;
	private boolean fastsuite;

	public static boolean isModLoaded(String modId) {
		return LoadingModList.get().getModFileById(modId) != null;
	}

	@Override
	public void onLoad(String mixinPackage) {
		boolean devEnv = !FMLEnvironment.production;
		customization = CustomizationServiceFinder.shouldEnable(LoadingModList.get().getMods());
		persistentCreativeInventory = customization || isModLoaded("persistentcreativeinventory") || devEnv;
		fastScrolling = isModLoaded("fastscroll") || devEnv;
		lavaClearView = isModLoaded("lavaclearview") || devEnv;
		fastsuite = customization && isModLoaded("fastsuite");
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.startsWith("snownee.kiwi.mixin.customization.fastsuite.")) {
			return fastsuite;
		}
		if (mixinClassName.startsWith("snownee.kiwi.mixin.customization.")) {
			return customization;
		}
		if (mixinClassName.equals("snownee.kiwi.mixin.client.CreativeModeInventoryScreenMixin")) {
			return persistentCreativeInventory;
		}
		if (mixinClassName.equals("snownee.kiwi.mixin.client.OptionInstanceMixin")) {
			return fastScrolling;
		}
		if (mixinClassName.equals("snownee.kiwi.mixin.client.FogRendererMixin") || mixinClassName.equals(
				"snownee.kiwi.mixin.client.ScreenEffectRendererMixin")) {
			return lavaClearView;
		}
		return true;
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

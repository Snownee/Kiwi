package snownee.kiwi.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.LoadingModList;
import snownee.kiwi.customization.CustomizationServiceFinder;

public class MixinPlugin implements IMixinConfigPlugin {
	private boolean customization;
	private boolean persistentCreativeInventory;
	private boolean fastScrolling;
	private boolean ksit;

	@Override
	public void onLoad(String mixinPackage) {
		customization = CustomizationServiceFinder.shouldEnable(LoadingModList.get().getMods());
		persistentCreativeInventory =
				customization || LoadingModList.get().getModFileById("persistentcreativeinventory") != null || !FMLEnvironment.production;
		fastScrolling = LoadingModList.get().getModFileById("fastscroll") != null || !FMLEnvironment.production;
		ksit = LoadingModList.get().getModFileById("ksit") != null;
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
		if (mixinClassName.startsWith("snownee.kiwi.mixin.sit.")) {
			return customization || ksit;
		}
		if (mixinClassName.equals("snownee.kiwi.mixin.client.CreativeModeInventoryScreenMixin")) {
			return persistentCreativeInventory;
		}
		if (mixinClassName.equals("snownee.kiwi.mixin.client.OptionInstanceMixin")) {
			return fastScrolling;
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

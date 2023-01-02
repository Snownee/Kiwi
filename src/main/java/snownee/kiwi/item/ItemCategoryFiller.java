package snownee.kiwi.item;

import java.util.List;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemCategoryFiller {

	void fillItemCategory(CreativeModeTab tab, FeatureFlagSet flags, boolean hasPermissions, List<ItemStack> items);

}

package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

@Mixin(Item.class)
public interface ItemAccess {

	@Accessor
	CreativeModeTab getCategory();

	@Accessor
	@Mutable
	void setCategory(CreativeModeTab category);

}

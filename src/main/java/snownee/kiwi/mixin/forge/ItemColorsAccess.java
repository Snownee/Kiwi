package snownee.kiwi.mixin.forge;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.Item;

@Mixin(ItemColors.class)
public interface ItemColorsAccess {
	@Accessor
	Map<Item, ItemColor> getItemColors();
}
package snownee.kiwi.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.ai.behavior.WorkAtComposter;
import net.minecraft.world.item.Item;

@Mixin(WorkAtComposter.class)
public interface WorkAtComposterAccess {

	@Accessor
	static List<Item> getCOMPOSTABLE_ITEMS() {
		throw new IllegalStateException();
	}

	@Accessor
	@Mutable
	static void setCOMPOSTABLE_ITEMS(List<Item> list) {
		throw new IllegalStateException();
	}

}

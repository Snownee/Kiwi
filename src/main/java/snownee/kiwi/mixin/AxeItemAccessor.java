package snownee.kiwi.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;

@Mixin(AxeItem.class)
public interface AxeItemAccessor {

	@Accessor
	static Map<Block, Block> getSTRIPPABLES() {
		throw new IllegalStateException();
	}

	@Accessor
	@Final
	@Mutable
	static void setSTRIPPABLES(Map<Block, Block> map) {
		throw new IllegalStateException();
	}

}

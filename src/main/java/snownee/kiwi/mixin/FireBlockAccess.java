package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;

@Mixin(FireBlock.class)
public interface FireBlockAccess {

	@Invoker
	void callSetFlammable(Block block, int encouragement, int flammability);

}

package snownee.kiwi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

@Mixin(BlockBehaviour.class)
public interface BlockAccess {

	@Accessor
	Material getMaterial();

}

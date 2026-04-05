package snownee.kiwi.mixin.customization;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(WallBlock.class)
public interface WallBlockAccessor {
	@Accessor("shapes")
	Function<BlockState, VoxelShape> kiwi$getShapes();

	@Mutable
	@Accessor("shapes")
	void kiwi$setShapes(Function<BlockState, VoxelShape> shapes);

	@Accessor("collisionShapes")
	Function<BlockState, VoxelShape> kiwi$getCollisionShapes();

	@Mutable
	@Accessor("collisionShapes")
	void kiwi$setCollisionShapes(Function<BlockState, VoxelShape> shapes);
}

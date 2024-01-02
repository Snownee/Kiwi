package snownee.kiwi.block.entity;

import java.util.Set;

import com.mojang.datafixers.types.Type;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class InheritanceBlockEntityType<T extends BlockEntity> extends BlockEntityType<T> {

	private final Class<? extends Block> clazz;

	public InheritanceBlockEntityType(FabricBlockEntityTypeBuilder.Factory<? extends T> factory, Class<? extends Block> clazz, Type<?> datafixer) {
		super(factory::create, Set.of(), datafixer);
		this.clazz = clazz;
	}

	@Override
	public boolean isValid(BlockState state) {
		return clazz.isInstance(state.getBlock());
	}

}
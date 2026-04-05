package snownee.kiwi.block.entity;

import java.util.Set;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class InheritanceBlockEntityType<T extends BlockEntity> extends BlockEntityType<T> {

	private final Class<? extends Block> clazz;
	private final boolean onlyOpCanSetNbt;

	public InheritanceBlockEntityType(
			BlockEntityType.BlockEntitySupplier<? extends T> factory,
			Class<? extends Block> clazz,
			boolean onlyOpCanSetNbt) {
		super(factory::create, Set.of(), onlyOpCanSetNbt);
		this.clazz = clazz;
		this.onlyOpCanSetNbt = onlyOpCanSetNbt;
	}

	@Override
	public boolean isValid(BlockState state) {
		return clazz.isInstance(state.getBlock());
	}

	@Override
	public boolean onlyOpCanSetNbt() {
		return onlyOpCanSetNbt;
	}

}

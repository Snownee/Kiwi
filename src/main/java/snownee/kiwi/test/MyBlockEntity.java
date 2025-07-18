package snownee.kiwi.test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import snownee.kiwi.block.entity.ModBlockEntity;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public class MyBlockEntity extends ModBlockEntity {

	public MyBlockEntity(BlockPos pos, BlockState state) {
		super(TestModule.FIRST_TILE.get(), pos, state);
		persistData = true;
	}

	@Override
	protected void readPacketData(ValueInput valueInput) {}

	@Override
	protected void writePacketData(ValueOutput valueOutput) {}


	@Override
	protected void loadAdditional(ValueInput valueInput) {
		readPacketData(valueInput);
		super.loadAdditional(valueInput);
	}

	@Override
	protected void saveAdditional(ValueOutput valueOutput) {
		writePacketData(valueOutput);
		super.saveAdditional(valueOutput);
	}
}
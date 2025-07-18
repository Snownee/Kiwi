package snownee.kiwi.test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import snownee.kiwi.block.entity.RetextureBlockEntity;

public class TexBlockEntity extends RetextureBlockEntity {

	public TexBlockEntity(BlockPos pos, BlockState state) {
		super(TestModule.TEX_TILE.get(), pos, state, "0");
	}

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

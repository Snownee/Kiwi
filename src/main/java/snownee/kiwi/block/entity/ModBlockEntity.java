package snownee.kiwi.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Base BlockEntity skeleton used by all BlockEntity. It contains several standardized
 * implementations regarding networking.
 */
public abstract class ModBlockEntity extends BlockEntity implements BlockEntityDataListener {
	public boolean persistData = false;

	public ModBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ValueInput valueInput) {
		super.onDataPacket(net, valueInput);
		readPacketData(valueInput);
	}

	// Used for syncing data at the time when the chunk is loaded
	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
		writePacketData(valueOutput);
		return valueOutput.buildResult();
	}

	/**
	 * Read data for server-client syncing.
	 *
	 * @param valueInput the data source
	 */
	protected abstract void readPacketData(ValueInput valueInput);

	/**
	 * Write data for server-client syncing. ONLY write the necessary data!
	 *
	 * @param valueOutput the data sink
	 */
	protected abstract void writePacketData(ValueOutput valueOutput);

	public void refresh() {
		if (level != null && !level.isClientSide) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, 11);
			setChanged();
		}
	}

}

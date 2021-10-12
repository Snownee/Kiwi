package snownee.kiwi.block.entity;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base BlockEntity skeleton used by all BlockEntity. It contains several standardized
 * implementations regarding networking.
 */
public abstract class BaseBlockEntity extends BlockEntity {
	public boolean persistData = false;

	public BaseBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public final ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(worldPosition, -1, writePacketData(new CompoundTag()));
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		readPacketData(pkt.getTag());
	}

	// Used for syncing data at the time when the chunk is loaded
	@Nonnull
	@Override
	public CompoundTag getUpdateTag() {
		return save(new CompoundTag());
	}

	// Used for syncing data at the time when the chunk is loaded
	@Override
	public void handleUpdateTag(CompoundTag tag) {
		load(tag);
	}

	/**
	 * Read data for server-client syncing.
	 *
	 * @param data
	 *            the data source
	 */
	protected abstract void readPacketData(CompoundTag data);

	/**
	 * Write data for server-client syncing. ONLY write the necessary data!
	 *
	 * @param data
	 *            the data sink
	 * @return the parameter, or delegate to super method
	 */
	@Nonnull
	protected abstract CompoundTag writePacketData(CompoundTag data);

	public void refresh() {
		if (hasLevel() && !level.isClientSide) {
			BlockState state = getBlockState();
			level.markAndNotifyBlock(worldPosition, level.getChunkAt(worldPosition), state, state, 11, 512);
		}
	}

}

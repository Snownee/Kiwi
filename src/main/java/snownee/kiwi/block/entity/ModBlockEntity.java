package snownee.kiwi.block.entity;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base BlockEntity skeleton used by all BlockEntity. It contains several standardized
 * implementations regarding networking.
 */
public abstract class ModBlockEntity extends BlockEntity {
	public boolean persistData = false;

	public ModBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		CompoundTag compoundtag = pkt.getTag();
		if (compoundtag != null)
			readPacketData(compoundtag);
	}

	// Used for syncing data at the time when the chunk is loaded
	@Nonnull
	@Override
	public CompoundTag getUpdateTag() {
		return writePacketData(new CompoundTag());
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
			level.sendBlockUpdated(worldPosition, state, state, 11);
			setChanged();
		}
	}

}

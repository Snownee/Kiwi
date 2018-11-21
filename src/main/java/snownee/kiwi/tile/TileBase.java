package snownee.kiwi.tile;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/**
 * Base TileEntity skeleton used by all TileEntity in Cuisine. It contains several standardized
 * implementations regarding networking.
 */
public abstract class TileBase extends TileEntity
{
    @Override
    public final SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.pos, -1, this.writePacketData(new NBTTagCompound()));
    }

    @Override
    public final void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet)
    {
        this.readPacketData(packet.getNbtCompound());
    }

    // Used for syncing data at the time when the chunk is loaded
    @Nonnull
    @Override
    public final NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    // Used for syncing data at the time when the chunk is loaded
    @Override
    public final void handleUpdateTag(NBTTagCompound tag)
    {
        this.readFromNBT(tag);
    }

    /**
     * Read data for server-client syncing.
     * @param data the data source
     */
    protected abstract void readPacketData(NBTTagCompound data);

    /**
     * Write data for server-client syncing. ONLY write the necessary data!
     * @param data the data sink
     * @return the parameter, or delegate to super method
     */
    @Nonnull
    protected abstract NBTTagCompound writePacketData(NBTTagCompound data);

    protected void refresh()
    {
        if (hasWorld() && !world.isRemote)
        {
            IBlockState state = world.getBlockState(pos);
            world.markAndNotifyBlock(pos, null, state, state, 11);
        }
    }

}

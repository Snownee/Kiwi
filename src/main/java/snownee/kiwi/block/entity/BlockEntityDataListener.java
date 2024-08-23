package snownee.kiwi.block.entity;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public interface BlockEntityDataListener extends IBlockEntityExtension {
	@Override
	void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider);
}

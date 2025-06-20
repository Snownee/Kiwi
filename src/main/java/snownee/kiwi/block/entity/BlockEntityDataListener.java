package snownee.kiwi.block.entity;

import net.minecraft.network.Connection;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;

public interface BlockEntityDataListener extends IBlockEntityExtension {
	@Override
	void onDataPacket(Connection net, ValueInput valueInput);
}

package snownee.kiwi.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface PacketMod
{

    void writeDataTo(ByteBuf buffer);

    void readDataFrom(ByteBuf buffer);

    @SideOnly(Side.CLIENT)
    void handleClient(EntityPlayerSP player);

    void handleServer(EntityPlayerMP player);
}

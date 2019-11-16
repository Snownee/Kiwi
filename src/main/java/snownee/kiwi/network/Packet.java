package snownee.kiwi.network;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public abstract class Packet
{
    public Packet()
    {
    }

    public void send(PacketTarget target)
    {
        NetworkChannel.INSTANCE.channel.send(target, this);
    }

    public void send()
    {

    }

    public static abstract class PacketHandler<T extends Packet>
    {
        public abstract void encode(T msg, PacketBuffer buffer);

        public abstract T decode(PacketBuffer buffer);

        public abstract void handle(T msg, Supplier<NetworkEvent.Context> ctx);
    }
}

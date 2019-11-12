package snownee.kiwi.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import snownee.kiwi.Kiwi;
import snownee.kiwi.network.Packet.PacketHandler;

public final class NetworkChannel
{
    private static final String PROTOCOL_VERSION = Integer.toString(1);
    public static final NetworkChannel INSTANCE = new NetworkChannel();

    private int nextIndex = 0;
    public final SimpleChannel channel;

    private NetworkChannel()
    {
        /* off */
        channel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(Kiwi.MODID, "main"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();
        /* on */
    }

    public static <T extends Packet> void register(Class<T> klass, PacketHandler<T> handler)
    {
        INSTANCE.channel.registerMessage(INSTANCE.nextIndex, klass, handler::encode, handler::decode, handler::handle);
        INSTANCE.nextIndex++;
    }
}

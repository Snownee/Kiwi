package snownee.kiwi.network;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import snownee.kiwi.network.Packet.PacketHandler;

public enum NetworkChannel {
    INSTANCE;

    private static final String PROTOCOL_VERSION = Integer.toString(1);

    private final Map<Class<?>, SimpleChannel> packet2channel = Maps.newConcurrentMap();
    private final Map<String, Pair<SimpleChannel, AtomicInteger>> channels = Maps.newConcurrentMap();

    private NetworkChannel() {}

    public static <T extends Packet> void register(Class<T> klass, PacketHandler<T> handler) {
        final String modid = ModLoadingContext.get().getActiveNamespace();
        if ("minecraft".equals(modid)) {
            throw new IllegalStateException("ModLoadingContext cannot detect modid while registering packet: " + klass);
        }
        Pair<SimpleChannel, AtomicInteger> pair = INSTANCE.channels.computeIfAbsent(modid, k -> {
            /* off */
            SimpleChannel channel = NetworkRegistry.ChannelBuilder
                    .named(new ResourceLocation(k, "main"))
                    .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                    .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                    .networkProtocolVersion(() -> PROTOCOL_VERSION)
                    .simpleChannel();
            /* on */
            return Pair.of(channel, new AtomicInteger());
        });
        INSTANCE.packet2channel.put(klass, pair.getKey());
        pair.getKey().registerMessage(pair.getValue().getAndIncrement(), klass, handler::encode, handler::decode, handler::handle);
    }

    public static SimpleChannel channel(Class<?> klass) {
        return INSTANCE.packet2channel.get(klass);
    }

    public static void send(PacketTarget target, Packet packet) {
        channel(packet.getClass()).send(target, packet);
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendToServer(Packet packet) {
        channel(packet.getClass()).sendToServer(packet);
    }
}

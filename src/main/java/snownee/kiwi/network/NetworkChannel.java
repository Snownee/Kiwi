package snownee.kiwi.network;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import snownee.kiwi.network.Packet.PacketHandler;

public enum NetworkChannel {
	INSTANCE;

	private static final String PROTOCOL_VERSION = Integer.toString(1);

	private final Map<Class<?>, SimpleChannel> packet2channel = Maps.newConcurrentMap();
	private final Map<ResourceLocation, Pair<SimpleChannel, AtomicInteger>> channels = Maps.newConcurrentMap();

	private NetworkChannel() {
	}

	public static <T extends Packet> void register(Class<T> klass, PacketHandler<T> handler) {
		register(klass, handler, "main");
	}

	/**
	 * @since 2.6.0
	 */
	public static <T extends Packet> void register(Class<T> klass, PacketHandler<T> handler, String channelName) {
		final String modid = ModLoadingContext.get().getActiveNamespace();
		if ("minecraft".equals(modid)) {
			throw new IllegalStateException("ModLoadingContext cannot detect modid while registering packet: " + klass);
		}
		ResourceLocation id = new ResourceLocation(modid, channelName);
		Pair<SimpleChannel, AtomicInteger> pair = INSTANCE.channels.computeIfAbsent(id, $ -> {
			/* off */
            SimpleChannel channel = NetworkRegistry.ChannelBuilder
                    .named(id)
                    .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                    .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                    .networkProtocolVersion(() -> PROTOCOL_VERSION)
                    .simpleChannel();
            /* on */
			return Pair.of(channel, new AtomicInteger());
		});
		INSTANCE.packet2channel.put(klass, pair.getKey());
		pair.getKey().registerMessage(pair.getValue().getAndIncrement(), klass, handler::encode, handler::decode, handler::handle, Optional.ofNullable(handler.direction()));
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

	public static final PacketDistributor<ServerPlayerEntity> ALL_EXCEPT = new PacketDistributor<>((dist, player) -> {
		return p -> getServer().getPlayerList().getPlayers().forEach(player2 -> {
			if (player.get() != player2) {
				player2.connection.netManager.sendPacket(p);
			}
		});
	}, NetworkDirection.PLAY_TO_CLIENT);

	private static MinecraftServer getServer() {
		return LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
	}

	public static void sendToAllExcept(ServerPlayerEntity player, Packet packet) {
		send(ALL_EXCEPT.with(() -> player), packet);
	}
}

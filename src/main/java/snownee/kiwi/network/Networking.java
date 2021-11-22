package snownee.kiwi.network;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.PacketDistributor.PacketTarget;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import snownee.kiwi.Kiwi;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.network.PacketHandler.Impl;

public final class Networking {
	private static final String protocol = Integer.toString(1);
	private static final SimpleChannel channel;
	private static final Map<ResourceLocation, PacketHandler> handlers = Maps.newHashMap();

	private Networking() {
	}

	static {
		/* off */
		channel = NetworkRegistry.ChannelBuilder
				.named(new ResourceLocation(Kiwi.MODID, "main"))
				.clientAcceptedVersions(protocol::equals)
				.serverAcceptedVersions(protocol::equals)
				.networkProtocolVersion(() -> protocol)
				.simpleChannel();
		/* on */
		channel.registerMessage(0, FriendlyByteBuf.class, Networking::encode, Networking::decode, Networking::handle);
	}

	private static void encode(FriendlyByteBuf msg, FriendlyByteBuf buf) {
		buf.writeBytes(msg);
	}

	private static FriendlyByteBuf decode(FriendlyByteBuf buf) {
		return buf;
	}

	private static void handle(FriendlyByteBuf msg, Supplier<NetworkEvent.Context> ctx) {
		ResourceLocation id = msg.readResourceLocation();
		PacketHandler handler = handlers.get(id);
		if (handler == null) {
			ctx.get().getNetworkManager().disconnect(new TextComponent("Illegal packet received, terminating connection"));
			throw new IllegalStateException("Invalid packet received, aborting connection");
		}
		Direction direction = handler.getDirection();
		if (direction != null) {
			NetworkHooks.validatePacketDirection(ctx.get().getDirection(), Optional.of(direction.value), ctx.get().getNetworkManager());
		}
		handler.receive($ -> ctx.get().enqueueWork($).thenApply($$ -> null), msg, ctx.get().getSender());
		ctx.get().setPacketHandled(true);
	}

	public static synchronized void registerHandler(ResourceLocation id, PacketHandler handler) {
		handlers.put(id, handler);
	}

	public static void send(PacketTarget target, FriendlyByteBuf buf) {
		channel.send(target, buf);
	}

	public static void sendToServer(FriendlyByteBuf buf) {
		channel.sendToServer(buf);
	}

	public static void sendToPlayer(ServerPlayer player, FriendlyByteBuf buf) {
		send(PacketDistributor.PLAYER.with(() -> player), buf);
	}

	public static final PacketDistributor<ServerPlayer> ALL_EXCEPT = new PacketDistributor<>((dist, player) -> (p -> Platform.getServer().getPlayerList().getPlayers().forEach(player2 -> {
		if (player.get() != player2) {
			player2.connection.connection.send(p);
		}
	})), NetworkDirection.PLAY_TO_CLIENT);

	public enum Direction {
		PLAY_TO_SERVER(NetworkDirection.PLAY_TO_SERVER),
		PLAY_TO_CLIENT(NetworkDirection.PLAY_TO_CLIENT),
		LOGIN_TO_SERVER(NetworkDirection.LOGIN_TO_SERVER),
		LOGIN_TO_CLIENT(NetworkDirection.LOGIN_TO_CLIENT);

		private final NetworkDirection value;

		Direction(NetworkDirection value) {
			this.value = value;
		}
	}

	public static void processClass(String className, String modId) throws Exception {
		Class<? extends PacketHandler.Impl> clazz = (Class<? extends Impl>) Class.forName(className);
		PacketHandler.Impl handler = clazz.getDeclaredConstructor().newInstance();
		handler.setModId(modId);
		registerHandler(handler.id, handler);
		try {
			Field field = clazz.getDeclaredField("I");
			field.set(null, handler);
		} catch (NoSuchFieldException e) {
		}
	}

}

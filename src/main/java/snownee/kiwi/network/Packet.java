package snownee.kiwi.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.PacketDistributor.PacketTarget;

public abstract class Packet {
	public Packet() {
	}

	public void send(PacketTarget target) {
		NetworkChannel.send(target, this);
	}

	/**
	 * @since 2.7.0
	 */
	public void send(ServerPlayer player) {
		send(PacketDistributor.PLAYER.with(() -> player));
	}

	public void send() {
	}

	public static abstract class PacketHandler<T extends Packet> {
		public abstract void encode(T msg, FriendlyByteBuf buffer);

		public abstract T decode(FriendlyByteBuf buffer);

		public abstract void handle(T msg, Supplier<NetworkEvent.Context> ctx);

		public NetworkDirection direction() {
			return null;
		}
	}
}

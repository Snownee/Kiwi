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

	public interface PacketHandler<T extends Packet> {
		void encode(T msg, FriendlyByteBuf buffer);

		T decode(FriendlyByteBuf buffer);

		void handle(T msg, Supplier<NetworkEvent.Context> ctx);

		default NetworkDirection direction() {
			return null;
		}
	}
}

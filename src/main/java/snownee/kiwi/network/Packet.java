package snownee.kiwi.network;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.PacketTarget;

public abstract class Packet {
	public Packet() {
	}

	public void send(PacketTarget target) {
		NetworkChannel.send(target, this);
	}

	/**
	 * @since 2.7.0
	 */
	public void send(ServerPlayerEntity player) {
		send(PacketDistributor.PLAYER.with(() -> player));
	}

	public void send() {
	}

	public static abstract class PacketHandler<T extends Packet> {
		public abstract void encode(T msg, PacketBuffer buffer);

		public abstract T decode(PacketBuffer buffer);

		public abstract void handle(T msg, Supplier<NetworkEvent.Context> ctx);
	}
}

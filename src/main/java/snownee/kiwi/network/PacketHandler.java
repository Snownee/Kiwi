package snownee.kiwi.network;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import snownee.kiwi.network.KiwiPacket.Direction;

public abstract class PacketHandler implements IPacketHandler {

	public ResourceLocation id;
	private Direction direction;

	void setModId(String modId) {
		KiwiPacket annotation = getClass().getDeclaredAnnotation(KiwiPacket.class);
		String v = annotation.value();
		if (v.contains(":")) {
			id = new ResourceLocation(v);
		} else {
			id = new ResourceLocation(modId, v);
		}
		direction = annotation.dir();
	}

	@Override
	public Direction getDirection() {
		return direction;
	}


	public void send(Stream<ServerPlayer> players, Consumer<FriendlyByteBuf> buf) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buf.accept(buffer);
		Packet<?> packet = ServerPlayNetworking.createS2CPacket(id, buffer);
		players.forEach($ -> $.connection.send(packet));
	}

	public void send(ServerPlayer player, Consumer<FriendlyByteBuf> buf) {
		send(Stream.of(player), buf);
	}

	public void sendToServer(Consumer<FriendlyByteBuf> buf) {
		FriendlyByteBuf buffer = PacketByteBufs.create();
		buf.accept(buffer);
		ClientPlayNetworking.send(id, buffer);
	}

	public void send(KPacketTarget target, Consumer<FriendlyByteBuf> buf) {
		target.send(this, buf);
	}

	@Deprecated
	public void send(Collection<ServerPlayer> players, Consumer<FriendlyByteBuf> buf) {
		send(players.stream(), buf);
	}

	@Deprecated
	public void sendAllExcept(ServerPlayer player, Consumer<FriendlyByteBuf> buf) {
		send(KPacketTarget.allExcept(player), buf);
	}

}

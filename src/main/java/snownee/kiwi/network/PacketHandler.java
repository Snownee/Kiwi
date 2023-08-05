package snownee.kiwi.network;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
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

	@Deprecated
	public void send(PacketTarget target, Consumer<FriendlyByteBuf> buf) {
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeResourceLocation(id);
		buf.accept(buffer);
		Networking.send(target, buffer);
	}

	public void send(ServerPlayer player, Consumer<FriendlyByteBuf> buf) {
		send(() -> PacketDistributor.PLAYER.with(() -> player), buf);
	}

	@Deprecated
	public void sendAllExcept(ServerPlayer player, Consumer<FriendlyByteBuf> buf) {
		send(KPacketTarget.allExcept(player), buf);
	}

	public void sendToServer(Consumer<FriendlyByteBuf> buf) {
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeResourceLocation(id);
		buf.accept(buffer);
		Networking.sendToServer(buffer);
	}

	public void send(KPacketTarget target, Consumer<FriendlyByteBuf> buf) {
		target.send(this, buf);
	}
}

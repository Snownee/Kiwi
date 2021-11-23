package snownee.kiwi.network;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.PacketDistributor.PacketTarget;

public abstract class PacketHandler implements IPacketHandler {

	public ResourceLocation id;
	private Networking.Direction direction;

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

	public Networking.Direction getDirection() {
		return direction;
	}

	public void send(PacketTarget target, Consumer<FriendlyByteBuf> buf) {
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeResourceLocation(id);
		buf.accept(buffer);
		Networking.send(target, buffer);
	}

	public void send(ServerPlayer player, Consumer<FriendlyByteBuf> buf) {
		send(PacketDistributor.PLAYER.with(() -> player), buf);
	}

	public void sendAllExcept(ServerPlayer player, Consumer<FriendlyByteBuf> buf) {
		send(Networking.ALL_EXCEPT.with(() -> player), buf);
	}

	public void sendToServer(Consumer<FriendlyByteBuf> buf) {
		FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer()).writeResourceLocation(id);
		buf.accept(buffer);
		Networking.sendToServer(buffer);
	}

}
package snownee.kiwi.test;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;
import snownee.kiwi.network.Packet;

public class MyPacket extends Packet {
	private int number;

	public MyPacket(int number) {
		this.number = number;
	}

	public static class Handler implements PacketHandler<MyPacket> {

		@Override
		public void encode(MyPacket msg, FriendlyByteBuf buffer) {
			buffer.writeVarInt(msg.number);
		}

		@Override
		public MyPacket decode(FriendlyByteBuf buffer) {
			return new MyPacket(buffer.readVarInt());
		}

		@Override
		public void handle(MyPacket message, Supplier<Context> ctx) {
			ctx.get().enqueueWork(() -> {
				System.out.println(message.number);
			});
			ctx.get().setPacketHandled(true);
		}

		@Override
		public NetworkDirection direction() {
			return NetworkDirection.PLAY_TO_CLIENT;
		}

	}
}

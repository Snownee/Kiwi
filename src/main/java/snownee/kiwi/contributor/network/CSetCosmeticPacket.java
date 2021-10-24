package snownee.kiwi.contributor.network;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.network.ClientPacket;
import snownee.kiwi.util.Util;

public class CSetCosmeticPacket extends ClientPacket {

	@Nullable
	private final ResourceLocation id;

	public CSetCosmeticPacket(@Nullable ResourceLocation id) {
		this.id = id;
	}

	public static class Handler implements PacketHandler<CSetCosmeticPacket> {

		@Override
		public void encode(CSetCosmeticPacket msg, FriendlyByteBuf buffer) {
			if (msg.id == null) {
				buffer.writeUtf("");
			} else {
				buffer.writeUtf(msg.id.toString());
			}
		}

		@Override
		public CSetCosmeticPacket decode(FriendlyByteBuf buffer) {
			ResourceLocation id = Util.RL(buffer.readUtf(32767));
			return new CSetCosmeticPacket(id);
		}

		@Override
		public void handle(CSetCosmeticPacket msg, Supplier<NetworkEvent.Context> ctx) {
			//            ctx.get().enqueueWork(() -> {
			Contributors.changeCosmetic(ctx.get().getSender(), msg.id);
			//            });
			ctx.get().setPacketHandled(true);
		}
	}

}

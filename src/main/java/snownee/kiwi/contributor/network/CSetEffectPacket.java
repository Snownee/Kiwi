package snownee.kiwi.contributor.network;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.network.ClientPacket;
import snownee.kiwi.util.Util;

public class CSetEffectPacket extends ClientPacket {

	@Nullable
	private final ResourceLocation id;

	public CSetEffectPacket(@Nullable ResourceLocation id) {
		this.id = id;
	}

	public static class Handler extends PacketHandler<CSetEffectPacket> {

		@Override
		public void encode(CSetEffectPacket msg, PacketBuffer buffer) {
			if (msg.id == null) {
				buffer.writeUtf("");
			} else {
				buffer.writeUtf(msg.id.toString());
			}
		}

		@Override
		public CSetEffectPacket decode(PacketBuffer buffer) {
			ResourceLocation id = Util.RL(buffer.readUtf(32767));
			return new CSetEffectPacket(id);
		}

		@Override
		public void handle(CSetEffectPacket msg, Supplier<Context> ctx) {
			//            ctx.get().enqueueWork(() -> {
			Contributors.changeEffect(ctx.get().getSender(), msg.id);
			//            });
			ctx.get().setPacketHandled(true);
		}
	}

}

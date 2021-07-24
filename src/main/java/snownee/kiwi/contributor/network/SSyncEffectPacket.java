package snownee.kiwi.contributor.network;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.network.NetworkChannel;
import snownee.kiwi.network.Packet;
import snownee.kiwi.util.Util;

public class SSyncEffectPacket extends Packet {

	private final Map<String, ResourceLocation> map;

	public SSyncEffectPacket(Map<String, ResourceLocation> map) {
		this.map = map;
	}

	public void sendExcept(ServerPlayer player) {
		NetworkChannel.sendToAllExcept(player, this);
	}

	public static class Handler extends PacketHandler<SSyncEffectPacket> {

		@Override
		public void encode(SSyncEffectPacket msg, FriendlyByteBuf buffer) {
			buffer.writeVarInt(msg.map.size());
			msg.map.forEach((k, v) -> {
				buffer.writeUtf(k);
				buffer.writeResourceLocation(v);
			});
		}

		@Override
		public SSyncEffectPacket decode(FriendlyByteBuf buffer) {
			ImmutableMap.Builder<String, ResourceLocation> builder = ImmutableMap.builder();
			int size = buffer.readVarInt();
			for (int i = 0; i < size; i++) {
				String k = buffer.readUtf();
				String v = buffer.readUtf();
				builder.put(k, Util.RL(v));
			}
			return new SSyncEffectPacket(builder.build());
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void handle(SSyncEffectPacket msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				Contributors.changeEffect(msg.map);
			});
			ctx.get().setPacketHandled(true);
		}

	}
}

package snownee.kiwi.contributor.network;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.contributor.Contributors;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;
import snownee.kiwi.util.Util;

@KiwiPacket
public record CSetCosmeticPacket(@Nullable ResourceLocation id) implements CustomPacketPayload {
	public static final Type<CSetCosmeticPacket> TYPE = CustomPacketPayload.createType("kiwi:set_cosmetic");

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static class Handler extends PlayPacketHandler<CSetCosmeticPacket> {
		@Override
		public CSetCosmeticPacket read(RegistryFriendlyByteBuf buf) {
			String s = buf.readUtf();
			return new CSetCosmeticPacket(s.isEmpty() ? null : Util.RL(s));
		}

		@Override
		public void write(CSetCosmeticPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeUtf(packet.id == null ? "" : packet.id.toString());
		}

		@Override
		public void handle(CSetCosmeticPacket packet, PayloadContext context) {
			context.execute(() -> Contributors.changeCosmetic(context.serverPlayer(), packet.id));
		}
	}
}

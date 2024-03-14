package snownee.kiwi.contributor.network;

import com.google.common.collect.ImmutableMap;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.contributor.ContributorsClient;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;

@KiwiPacket
public record SSyncCosmeticPacket(ImmutableMap<String, ResourceLocation> data) implements CustomPacketPayload {
	public static final Type<SSyncCosmeticPacket> TYPE = CustomPacketPayload.createType("kiwi:sync_cosmetic");

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static class Handler extends PlayPacketHandler<SSyncCosmeticPacket> {
		@Override
		public SSyncCosmeticPacket read(RegistryFriendlyByteBuf buf) {
			int size = buf.readVarInt();
			ImmutableMap.Builder<String, ResourceLocation> builder = ImmutableMap.builderWithExpectedSize(size);
			for (int i = 0; i < size; i++) {
				builder.put(buf.readUtf(), buf.readResourceLocation());
			}
			return new SSyncCosmeticPacket(builder.build());
		}

		@Override
		public void write(SSyncCosmeticPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeVarInt(packet.data.size());
			packet.data.forEach((k, v) -> {
				buf.writeUtf(k);
				buf.writeResourceLocation(v);
			});
		}

		@Override
		public void handle(SSyncCosmeticPacket packet, PayloadContext context) {
			context.execute(() -> ContributorsClient.changeCosmetic(packet.data()));
		}
	}
}

package snownee.kiwi.contributor.network;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.Kiwi;
import snownee.kiwi.contributor.ContributorsClient;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;

@KiwiPacket
public record SSyncCosmeticPacket(ImmutableMap<String, ResourceLocation> data) implements CustomPacketPayload {
	public static final Type<SSyncCosmeticPacket> TYPE = new CustomPacketPayload.Type<>(Kiwi.id("sync_cosmetic"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static class Handler implements PlayPacketHandler<SSyncCosmeticPacket> {
		public static final StreamCodec<RegistryFriendlyByteBuf, SSyncCosmeticPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.map(Maps::newHashMapWithExpectedSize, ByteBufCodecs.STRING_UTF8, ResourceLocation.STREAM_CODEC)
						.map(ImmutableMap::copyOf, Maps::newHashMap),
				SSyncCosmeticPacket::data,
				SSyncCosmeticPacket::new
		);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SSyncCosmeticPacket> streamCodec() {
			return STREAM_CODEC;
		}

		@Override
		public void handle(SSyncCosmeticPacket packet, PayloadContext context) {
			context.execute(() -> ContributorsClient.changeCosmetic(packet.data()));
		}
	}
}

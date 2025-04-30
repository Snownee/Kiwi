package snownee.kiwi.customization.network;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.Kiwi;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;

@KiwiPacket
public record SItemPopTimePacket(int slot, int popTime) implements CustomPacketPayload {

	public static final Type<SItemPopTimePacket> TYPE = new Type<>(Kiwi.id("item_pop_item"));

	@Override
	public Type<SItemPopTimePacket> type() {
		return TYPE;
	}

	public static final class Handler implements PlayPacketHandler<SItemPopTimePacket> {

		public static final StreamCodec<RegistryFriendlyByteBuf, SItemPopTimePacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT,
				SItemPopTimePacket::slot,
				ByteBufCodecs.VAR_INT,
				SItemPopTimePacket::popTime,
				SItemPopTimePacket::new);

		@Override
		public void handle(SItemPopTimePacket packet, PayloadContext context) {
			context.execute(() -> {
				ItemStack item = Objects.requireNonNull(Minecraft.getInstance().player).getInventory().getItem(packet.slot);
				if (!item.isEmpty()) {
					item.setPopTime(packet.popTime);
				}
			});
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SItemPopTimePacket> streamCodec() {
			return STREAM_CODEC;
		}
	}

	public static boolean send(ServerPlayer player, int slot) {
		if (!Inventory.isHotbarSlot(slot)) {
			return false;
		}
		ItemStack item = player.getInventory().getItem(slot);
		if (!item.isEmpty() && item.getPopTime() > 0) {
			KPacketSender.send(new SItemPopTimePacket(slot, item.getPopTime()), player);
			return true;
		}
		return false;
	}

}

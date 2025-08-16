package snownee.kiwi.customization.network;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket(value = "item_pop_item", dir = KiwiPacket.Direction.PLAY_TO_CLIENT)
public class SItemPopTimePacket extends PacketHandler {
	public static SItemPopTimePacket I;

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(
			Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor,
			FriendlyByteBuf buf,
			@Nullable ServerPlayer sender) {
		int slot = buf.readVarInt();
		int popTime = buf.readVarInt();
		return executor.apply(() -> {
			ItemStack item = Objects.requireNonNull(Minecraft.getInstance().player).getInventory().getItem(slot);
			if (!item.isEmpty()) {
				item.setPopTime(popTime);
			}
		});
	}

	public static boolean send(ServerPlayer player, int slot) {
		if (!Inventory.isHotbarSlot(slot)) {
			return false;
		}
		ItemStack item = player.getInventory().getItem(slot);
		if (!item.isEmpty() && item.getPopTime() > 0) {
			I.send(
					player, buf -> {
						buf.writeVarInt(slot);
						buf.writeVarInt(item.getPopTime());
					});
			return true;
		}
		return false;
	}

}

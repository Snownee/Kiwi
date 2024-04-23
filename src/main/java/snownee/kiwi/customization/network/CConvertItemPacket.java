package snownee.kiwi.customization.network;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.block.family.BlockFamily;
import snownee.kiwi.util.KHolder;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;

@KiwiPacket(value = "convert_item", dir = KiwiPacket.Direction.PLAY_TO_SERVER)
public class CConvertItemPacket extends PacketHandler {
	public static CConvertItemPacket I;

	public static void send(boolean inContainer, int slot, KHolder<BlockFamily> family, Item from, Item to, boolean convertOne) {
		I.sendToServer(buf -> {
			buf.writeBoolean(inContainer);
			buf.writeBoolean(convertOne);
			buf.writeVarInt(slot);
			buf.writeResourceLocation(family.key());
			buf.writeId(BuiltInRegistries.ITEM, from);
			buf.writeId(BuiltInRegistries.ITEM, to);
		});
	}

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(
			Function<Runnable, CompletableFuture<FriendlyByteBuf>> function,
			FriendlyByteBuf buf,
			@Nullable ServerPlayer player) {
		boolean inContainer = buf.readBoolean();
		boolean convertOne = buf.readBoolean();
		int slotIndex = buf.readVarInt();
		ResourceLocation familyId = buf.readResourceLocation();
		Item from = buf.readById(BuiltInRegistries.ITEM);
		Item to = buf.readById(BuiltInRegistries.ITEM);
		if (from == null || to == null) {
			return null;
		}
		if (from.equals(to)) {
			return null;
		}
		return function.apply(() -> {
			Objects.requireNonNull(player);
			BlockFamily family = BlockFamilies.get(familyId);
			if (family == null || !family.quickSwitch() || !family.contains(from) || !family.contains(to)) {
				return;
			}
			ItemStack sourceItem;
			Slot slot = null;
			try {
				if (slotIndex == -500) {
					if (player.isCreative()) {
						sourceItem = from.getDefaultInstance();
					} else {
						return;
					}
				} else if (inContainer) {
					slot = player.containerMenu.slots.get(slotIndex);
					if (!slot.allowModification(player)) {
						return;
					}
					sourceItem = slot.getItem();
				} else {
					sourceItem = player.getInventory().getItem(slotIndex);
				}
			} catch (Exception e) {
				return;
			}
			if (!sourceItem.is(from)) {
				return;
			}
			boolean skipSettingSlot = false;
			ItemStack newItem = to.getDefaultInstance();
			newItem.setPopTime(5);
			if (convertOne) {
				if (!player.isCreative()) {
					sourceItem.shrink(1);
				}
				if (!sourceItem.isEmpty()) {
					addToPlayer(player, newItem, !inContainer);
					skipSettingSlot = true;
				}
			} else {
				newItem.setCount(sourceItem.getCount()); // check max stack size?
			}
			if (slotIndex != -500 && !skipSettingSlot) {
				try {
					if (inContainer) {
						if (!slot.mayPlace(newItem)) {
							return;
						}
						slot.setByPlayer(newItem);
					} else {
						player.getInventory().setItem(slotIndex, newItem);
					}
				} catch (Exception e) {
					return;
				}
			}
			playPickupSound(player);
			player.containerMenu.broadcastChanges();
		});
	}

	private void addToPlayer(ServerPlayer player, ItemStack itemStack, boolean nextToSelected) {
		Inventory inventory = player.getInventory();
		IntStream intStream = IntStream.range(0, 9);
		if (nextToSelected) {
			IntStream leftAndRight = IntStream.of(inventory.selected + 1, inventory.selected - 1);
			intStream = IntStream.concat(leftAndRight, intStream);
		}
		int slot = intStream.filter(Inventory::isHotbarSlot).filter(i -> {
			ItemStack stack = inventory.getItem(i);
			if (stack.isEmpty()) {
				return true;
			}
			return stack.getCount() < stack.getMaxStackSize() && ItemStack.isSameItemSameTags(stack, itemStack);
		}).findFirst().orElse(-1);
		if (!player.getInventory().add(slot, itemStack) && !player.isCreative()) {
			player.drop(itemStack, true);
		}
	}

	public static void playPickupSound(Player player) {
		player.level().playSound(
				player.isLocalPlayer() ? player : null,
				player.getX(),
				player.getY(),
				player.getZ(),
				SoundEvents.ITEM_PICKUP,
				SoundSource.PLAYERS,
				0.2F,
				((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
	}
}

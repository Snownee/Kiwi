package snownee.kiwi.customization.network;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.block.family.BlockFamily;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PacketHandler;
import snownee.kiwi.util.KHolder;

@KiwiPacket(value = "convert_item", dir = KiwiPacket.Direction.PLAY_TO_SERVER)
public class CConvertItemPacket extends PacketHandler {
	public static final int MAX_STEPS = 4;
	public static CConvertItemPacket I;

	public static void send(boolean inContainer, int slot, Entry entry, Item from, Action action) {
		I.sendToServer(buf -> {
			buf.writeBoolean(inContainer);
			buf.writeEnum(action);
			buf.writeVarInt(slot);
			buf.writeId(BuiltInRegistries.ITEM, from);
			buf.writeVarInt(entry.steps().size());
			for (Pair<ResourceLocation, Item> step : entry.steps()) {
				buf.writeResourceLocation(step.getFirst());
				buf.writeId(BuiltInRegistries.ITEM, step.getSecond());
			}
		});
	}

	@Override
	public CompletableFuture<FriendlyByteBuf> receive(
			Function<Runnable, CompletableFuture<FriendlyByteBuf>> executor,
			FriendlyByteBuf buf,
			@Nullable ServerPlayer player) {
		boolean inContainer = buf.readBoolean();
		Action action = buf.readEnum(Action.class);
		int slotIndex = buf.readVarInt();
		Item from = buf.readById(BuiltInRegistries.ITEM);
		if (from == null) {
			return null;
		}
		int size = buf.readVarInt();
		List<Pair<ResourceLocation, Item>> steps = Lists.newArrayListWithExpectedSize(size);
		for (int i = 0; i < size; i++) {
			ResourceLocation familyId = buf.readResourceLocation();
			Item item = buf.readById(BuiltInRegistries.ITEM);
			if (item == null) {
				return null;
			}
			steps.add(Pair.of(familyId, item));
		}
		if (steps.isEmpty() || steps.size() > MAX_STEPS) {
			return null;
		}
		Item to = steps.get(steps.size() - 1).getSecond();
		if (action == Action.CONVERT_FAMILY && inContainer) {
			return null;
		}
		return executor.apply(() -> {
			Objects.requireNonNull(player);
			Item item = from;
			int index = 0;
			float ratio = 1;
			for (Pair<ResourceLocation, Item> step : steps) {
				BlockFamily family = BlockFamilies.get(step.getFirst());
				if (family == null || !family.switchAttrs().enabled() || !family.contains(item) || !family.contains(step.getSecond())) {
					return;
				}
				if (!family.switchAttrs().cascading() && index != steps.size() - 1) {
					return;
				}
				if (!player.isCreative()) {
					ratio *= BlockFamilies.getConvertRatio(item) / BlockFamilies.getConvertRatio(step.getSecond());
				}
				item = step.getSecond();
				++index;
			}
			ItemStack sourceItem;
			Slot slot = null;
			Inventory playerInventory = player.getInventory();
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
					sourceItem = playerInventory.getItem(slotIndex);
				}
			} catch (Exception e) {
				return;
			}
			if (!sourceItem.is(from)) {
				return;
			}
			if (action == Action.CONVERT_FAMILY) {
				convertFamily(player, to, slotIndex, ratio);
				return;
			}
			boolean skipSettingSlot = false;
			ItemStack newItem;
			int inventorySwap = Integer.MIN_VALUE;
			if (ratio >= 1) {
				newItem = to.getDefaultInstance();
			} else if (action == Action.CONVERT_ONE) {
				return;
			} else {
				for (int i = 0; i < playerInventory.getContainerSize(); i++) {
					ItemStack stack = playerInventory.getItem(i);
					if (stack.is(to)) {
						inventorySwap = i;
						break;
					}
				}
				if (inventorySwap == Integer.MIN_VALUE) {
					return;
				}
				newItem = playerInventory.getItem(inventorySwap);
			}
			int ratioInt = Mth.floor(ratio);
			if (action == Action.CONVERT_ONE) {
				if (!player.isCreative()) {
					sourceItem.shrink(1);
					newItem.setCount(ratioInt);
				}
				if (!sourceItem.isEmpty()) {
					addToPlayer(player, newItem, !inContainer);
					skipSettingSlot = true;
				}
			} else if (inventorySwap == Integer.MIN_VALUE) {
				int maxSize = newItem.getMaxStackSize();
				int count = Math.min(sourceItem.getCount(), maxSize / ratioInt);
				newItem.setCount(count * ratioInt);
				if (!player.isCreative()) {
					sourceItem.shrink(count);
				}
			}
			if (slotIndex != -500 && !skipSettingSlot) {
				try {
					if (inContainer) {
						if (!slot.mayPlace(newItem)) {
							return;
						}
						slot.setByPlayer(newItem);
					} else {
						newItem.setPopTime(Inventory.POP_TIME_DURATION);
						playerInventory.setItem(slotIndex, newItem);
					}
				} catch (Exception e) {
					return;
				}
			}
			if (inventorySwap != Integer.MIN_VALUE) {
				playerInventory.setItem(inventorySwap, sourceItem);
			} else if (!skipSettingSlot && !player.isCreative()) {
				addToPlayer(player, sourceItem.copy(), !inContainer);
			}
			broadcastChanges(player);
			playPickupSound(player);
			player.containerMenu.broadcastChanges();
		});
	}

	private static void broadcastChanges(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		boolean success = false;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			success |= SItemPopTimePacket.send(player, i);
		}
		if (success) {
			playPickupSound(player);
			player.containerMenu.broadcastChanges();
		}
	}

	public static void convertFamily(ServerPlayer player, Item to, int slotIndex, float ratio) {
		Set<Item> set = BlockFamilies.findQuickSwitch(to, player.isCreative()).stream()
				.map(KHolder::value)
				.flatMap(BlockFamily::items)
				.filter(Predicate.not(to::equals))
				.collect(Collectors.toSet());
		Inventory inventory = player.getInventory();
		boolean success = false;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!set.contains(stack.getItem())) {
				continue;
			}
			success = true;
			inventory.setItem(i, ItemStack.EMPTY);
			ItemStack newItem = to.getDefaultInstance();
			newItem.setPopTime(Inventory.POP_TIME_DURATION);
			int newCount = Mth.floor(stack.getCount() * ratio);
			while (newCount > 0) {
				int count = Math.min(newCount, newItem.getMaxStackSize());
				newItem.setCount(count);
				newCount -= count;
				if (!inventory.add(slotIndex, newItem) && !inventory.add(newItem)) {
					player.drop(newItem, true);
				}
				if (newCount > 0) {
					newItem = newItem.copy();
				}
			}
		}
		if (success) {
			broadcastChanges(player);
		}
	}

	private static void addToPlayer(ServerPlayer player, ItemStack itemStack, boolean nextToSelected) {
		Inventory inventory = player.getInventory();
		IntStream intStream = IntStream.range(0, 9);
		if (nextToSelected) {
			IntStream leftAndRight = IntStream.of(inventory.selected, inventory.selected + 1, inventory.selected - 1);
			intStream = IntStream.concat(leftAndRight, intStream);
		}
		int slot = intStream.filter(Inventory::isHotbarSlot).filter(i -> {
			ItemStack stack = inventory.getItem(i);
			if (stack.isEmpty()) {
				return true;
			}
			return stack.getCount() < stack.getMaxStackSize() && ItemStack.isSameItemSameTags(stack, itemStack);
		}).findFirst().orElse(-1);
		if (!inventory.add(slot, itemStack) && !inventory.add(itemStack) && !player.isCreative()) {
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

	public record Group(LinkedHashSet<CConvertItemPacket.Entry> entries) {
		public Group() {
			this(Sets.newLinkedHashSet());
		}
	}

	public record Entry(float ratio, List<Pair<ResourceLocation, Item>> steps) {
		public Entry(float ratio) {
			this(ratio, Lists.newArrayList());
		}

		public Item item() {
			return steps.get(steps.size() - 1).getSecond();
		}
	}

	public enum Action {
		CONVERT_ALL,
		CONVERT_ONE,
		CONVERT_FAMILY;

		private static final IntFunction<Action> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
	}
}

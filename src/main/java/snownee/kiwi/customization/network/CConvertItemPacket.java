package snownee.kiwi.customization.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
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
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiCommonConfig;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.block.family.BlockFamily;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;
import snownee.kiwi.util.KHolder;

@KiwiPacket
public record CConvertItemPacket(
		boolean inContainer,
		Action action,
		int slot,
		Holder<Item> from,
		Entry entry
) implements CustomPacketPayload {

	public static final Type<CConvertItemPacket> TYPE = new Type<>(Kiwi.id("convert_item"));

	public static final int MAX_STEPS = 4;

	@Override
	public Type<CConvertItemPacket> type() {
		return TYPE;
	}

	public CConvertItemPacket(boolean inContainer, int slot, Entry entry, Item from, Action action) {
		//noinspection deprecation
		this(inContainer, action, slot, from.builtInRegistryHolder(), entry);
	}

	public static class Handler implements PlayPacketHandler<CConvertItemPacket> {

		public static final StreamCodec<RegistryFriendlyByteBuf, CConvertItemPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.BOOL, CConvertItemPacket::inContainer,
				Action.STREAM_CODEC, CConvertItemPacket::action,
				ByteBufCodecs.VAR_INT, CConvertItemPacket::slot,
				ByteBufCodecs.holderRegistry(Registries.ITEM), CConvertItemPacket::from,
				Entry.STREAM_CODEC, CConvertItemPacket::entry,
				CConvertItemPacket::new
		);

		@Override
		public void handle(CConvertItemPacket packet, PayloadContext context) {
			var player = context.serverPlayer();
			if (KiwiCommonConfig.kSwitchCreativeOnly && !player.isCreative()) {
				return;
			}
			var action = packet.action;
			var inContainer = packet.inContainer;
			var slotIndex = packet.slot;
			var from = packet.from.value();
			var steps = packet.entry.steps;
			if (steps.isEmpty() || steps.size() > MAX_STEPS) {
				return;
			}
			if (action == Action.CONVERT_FAMILY && inContainer) {
				return;
			}
			Item to = steps.getLast().getSecond();
			context.execute(() -> {
				Item item = from;
				int index = 0;
				float ratio = 1;
				for (Pair<Identifier, Item> step : steps) {
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

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CConvertItemPacket> streamCodec() {
			return STREAM_CODEC;
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
				return stack.getCount() < stack.getMaxStackSize() && ItemStack.isSameItemSameComponents(stack, itemStack);
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
	}

	public record Group(List<CConvertItemPacket.Entry> entries) {
		public Group() {
			this(Lists.newArrayList());
		}
	}

	public record Entry(float ratio, List<Pair<Identifier, Item>> steps) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Pair<Identifier, Item>> ENTRY_PAIR_STREAM_CODEC = StreamCodec.composite(
				Identifier.STREAM_CODEC, Pair::getFirst,
				ByteBufCodecs.registry(Registries.ITEM), Pair::getSecond,
				Pair::of);

		public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.FLOAT, Entry::ratio,
				ByteBufCodecs.collection(ArrayList::new, ENTRY_PAIR_STREAM_CODEC), Entry::steps,
				Entry::new
		);

		public Entry(float ratio) {
			this(ratio, Lists.newArrayList());
		}

		public Item item() {
			return steps.getLast().getSecond();
		}
	}

	public enum Action {
		CONVERT_ALL,
		CONVERT_ONE,
		CONVERT_FAMILY;

		private static final IntFunction<Action> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
		public static final StreamCodec<ByteBuf, Action> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
	}
}

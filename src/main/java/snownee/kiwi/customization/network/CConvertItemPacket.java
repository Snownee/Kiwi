package snownee.kiwi.customization.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
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
	public static final int SLOT_UNKNOWN_SOURCE = -500;
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
			if (KiwiCommonConfig.kSwitchCreativeOnly && !player.hasInfiniteMaterials()) {
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
				Inventory playerInventory = player.getInventory();
				ItemStack sourceItem;
				{
					Item item = from;
					int index = 0;
					for (Pair<Identifier, Item> step : steps) {
						BlockFamily family = BlockFamilies.get(step.getFirst());
						if (family == null || !family.switchAttrs().enabled() || !family.contains(item) ||
								!family.contains(step.getSecond())) {
							return;
						}
						if (!family.switchAttrs().cascading() && index != steps.size() - 1) {
							return;
						}
						item = step.getSecond();
						++index;
					}
					try {
						if (slotIndex == SLOT_UNKNOWN_SOURCE) {
							if (player.hasInfiniteMaterials()) {
								sourceItem = from.getDefaultInstance();
							} else {
								return;
							}
						} else if (inContainer) {
							Slot slot = player.containerMenu.slots.get(slotIndex);
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
				}
				if (!sourceItem.is(from)) {
					return;
				}
				if (action == Action.CONVERT_FAMILY) {
					convertFamily(player, to, slotIndex);
					return;
				}
				int consumedCount = action == Action.CONVERT_ONE ? 1 : sourceItem.count();
				long matValue = BlockFamilies.getMatValue(from) * consumedCount;
				int newCount = player.hasInfiniteMaterials() ? consumedCount : (int) (matValue / BlockFamilies.getMatValue(to));
				if (newCount < 1) {
					return;
				}
				if (player.hasInfiniteMaterials() && action == Action.CONVERT_ALL && slotIndex != SLOT_UNKNOWN_SOURCE) {
					sourceItem.shrink(consumedCount);
				} else if (!player.hasInfiniteMaterials()) {
					sourceItem.shrink(consumedCount);
				}
				addToPlayer(player, to.getDefaultInstance(), newCount, true);
				broadcastChanges(player);
			});
		}

		private static void broadcastChanges(ServerPlayer player) {
			player.containerMenu.broadcastChanges();
			Inventory inventory = player.getInventory();
			boolean success = false;
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				success |= SItemPopTimePacket.send(player, i);
			}
			if (success) {
				playPickupSound(player);
			}
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CConvertItemPacket> streamCodec() {
			return STREAM_CODEC;
		}

		private static void convertFamily(ServerPlayer player, Item to, int slotIndex) {
			Set<Item> set = BlockFamilies.findQuickSwitch(to, player.hasInfiniteMaterials()).stream()
					.map(KHolder::value)
					.flatMap(BlockFamily::items)
					.collect(Collectors.toSet());
			Inventory inventory = player.getInventory();
			long matValue = 0;
			for (int i = 0; i < inventory.getContainerSize(); i++) {
				ItemStack stack = inventory.getItem(i);
				if (!set.contains(stack.getItem())) {
					continue;
				}
				inventory.setItem(i, ItemStack.EMPTY);
				matValue += BlockFamilies.getMatValue(stack);
			}
			if (matValue == 0) {
				return;
			}
			ItemStack itemStack = to.getDefaultInstance();
			if (player.hasInfiniteMaterials()) {
				itemStack.setPopTime(Inventory.POP_TIME_DURATION);
				inventory.setItem(slotIndex, itemStack);
			} else {
				int count = (int) (matValue / BlockFamilies.getMatValue(to));
				addToPlayer(player, itemStack, count, true);
			}
			broadcastChanges(player);
		}

		private static void addToPlayer(ServerPlayer player, ItemStack template, int count, boolean nextToSelected) {
			if (count == 0) {
				return;
			}
			Inventory inventory = player.getInventory();

			IntStream intStream = IntStream.range(0, 9);
			if (nextToSelected) {
				IntStream leftAndRight = IntStream.of(
						inventory.getSelectedSlot(),
						inventory.getSelectedSlot() + 1,
						inventory.getSelectedSlot() - 1);
				intStream = IntStream.concat(leftAndRight, intStream);
			}
			int[] slots = intStream.filter(Inventory::isHotbarSlot).toArray();

			while (count > 0) {
				int selectedSlot = -1;
				int singleCount = Math.min(count, template.getMaxStackSize());
				for (int slot : slots) {
					ItemStack itemInSlot = inventory.getItem(slot);
					if (itemInSlot.isEmpty()) {
						selectedSlot = slot;
						break;
					}
					if (itemInSlot.getMaxStackSize() > itemInSlot.count() && ItemStack.isSameItemSameComponents(itemInSlot, template)) {
						selectedSlot = slot;
						singleCount = Math.min(singleCount, itemInSlot.getMaxStackSize() - itemInSlot.count());
						break;
					}
				}

				ItemStack itemStack = template.copyWithCount(singleCount);
				itemStack.setPopTime(Inventory.POP_TIME_DURATION);
				count -= singleCount;

				if (!inventory.add(selectedSlot, itemStack) && !inventory.add(itemStack)) {
					player.drop(itemStack, true);
				}
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

	public record Group(List<Entry> entries) {
		public Group() {
			this(Lists.newArrayList());
		}
	}

	public record Entry(List<Pair<Identifier, Item>> steps) {
		public static final StreamCodec<RegistryFriendlyByteBuf, Pair<Identifier, Item>> ENTRY_PAIR_STREAM_CODEC = StreamCodec.composite(
				Identifier.STREAM_CODEC, Pair::getFirst,
				ByteBufCodecs.registry(Registries.ITEM), Pair::getSecond,
				Pair::of);

		public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.collection(ArrayList::new, ENTRY_PAIR_STREAM_CODEC),
				Entry::steps,
				Entry::new
		);

		public Entry() {
			this(Lists.newArrayList());
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

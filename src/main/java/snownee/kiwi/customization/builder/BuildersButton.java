package snownee.kiwi.customization.builder;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import snownee.kiwi.customization.CustomizationClient;
import snownee.kiwi.customization.block.family.BlockFamilies;
import snownee.kiwi.customization.block.family.BlockFamily;
import snownee.kiwi.customization.network.CApplyBuilderRulePacket;
import snownee.kiwi.customization.network.CConvertItemPacket;
import snownee.kiwi.util.ClientProxy;
import snownee.kiwi.util.KHolder;

public class BuildersButton {
	private static final BuilderModePreview PREVIEW_RENDERER = new BuilderModePreview();

	public static BuilderModePreview getPreviewRenderer() {
		return PREVIEW_RENDERER;
	}

	private static boolean builderMode;

	public static boolean isBuilderModeOn() {
		if (builderMode && CustomizationClient.buildersButtonKey.isUnbound()) {
			builderMode = false;
		}
		return builderMode;
	}

	public static boolean onLongPress() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || player.isSpectator()) { //TODO automatically disable builder mode
			return false;
		}
		builderMode = !builderMode;
		RandomSource random = RandomSource.create();
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
				SoundEvents.EXPERIENCE_ORB_PICKUP,
				(random.nextFloat() - random.nextFloat()) * 0.35F + 0.9F));
		return true;
	}

	public static boolean onDoublePress() {
		return false;
	}

	public static boolean onShortPress() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null) {
			return false;
		}
		Screen screen = mc.screen;
		if (screen instanceof ConvertScreen) {
			screen.onClose();
			return true;
		}
		if (screen instanceof AbstractContainerScreen<?> containerScreen && containerScreen.getMenu().getCarried().isEmpty()) {
			Slot slot = ClientProxy.getSlotUnderMouse(containerScreen);
			if (slot == null || !slot.hasItem() || !slot.allowModification(player)) {
				return false;
			}
			if (screen instanceof CreativeModeInventoryScreen && slot.container != player.getInventory()) {
				return false;
			}
			List<CConvertItemPacket.Group> groups = findConvertGroups(player, slot.getItem());
			if (groups.isEmpty()) {
				return false;
			}
			ClientProxy.pushScreen(mc, new ConvertScreen(screen, slot, slot.index, groups));
			return true;
		}
		if (screen != null) {
			return false;
		}
		List<CConvertItemPacket.Group> groups = findConvertGroups(player, player.getMainHandItem());
		if (!groups.isEmpty()) {
			mc.setScreen(new ConvertScreen(null, null, player.getInventory().selected, groups));
			return true;
		}
		groups = findConvertGroups(player, player.getOffhandItem());
		if (!groups.isEmpty()) {
			mc.setScreen(new ConvertScreen(null, null, Inventory.SLOT_OFFHAND, groups));
			return true;
		}
		return false;
	}

	public static List<CConvertItemPacket.Group> findConvertGroups(Player player, ItemStack itemStack) {
		List<KHolder<BlockFamily>> families = BlockFamilies.findQuickSwitch(itemStack.getItem(), player.isCreative());
		if (families.isEmpty()) {
			return List.of();
		}
		List<CConvertItemPacket.Group> groups = Lists.newArrayListWithExpectedSize(families.size());
		Set<Item> addedItems = Sets.newHashSet();
		float ratio = BlockFamilies.getConvertRatio(itemStack.getItem());
		for (KHolder<BlockFamily> family : families) {
			CConvertItemPacket.Group group = new CConvertItemPacket.Group();
			boolean cascading = family.value().switchAttrs().cascading();
			List<CConvertItemPacket.Entry> unresolved = cascading ? Lists.newLinkedList() : List.of();
			Set<BlockFamily> iteratedFamilies = cascading ? Sets.newHashSet(family.value()) : Set.of();
			Set<Item> iteratedItems = cascading ? Sets.newHashSet() : Set.of();
			for (Item item : family.value().items().toList()) {
				float convertRatio = BlockFamilies.getConvertRatio(item);
				CConvertItemPacket.Entry entry = new CConvertItemPacket.Entry(ratio / convertRatio);
				Pair<KHolder<BlockFamily>, Item> pair = Pair.of(family, item);
				entry.steps().add(pair);
				if (cascading) {
					unresolved.add(entry);
					iteratedItems.add(item);
				}
				if (!addedItems.contains(item)) {
					group.entries().add(entry);
				}
				addedItems.add(item);
			}
			while (!unresolved.isEmpty()) {
				CConvertItemPacket.Entry parentEntry = unresolved.remove(0);
				Pair<KHolder<BlockFamily>, Item> lastStep = parentEntry.steps().get(parentEntry.steps().size() - 1);
				Item lastItem = lastStep.getSecond();
				ratio = BlockFamilies.getConvertRatio(lastItem);
				for (KHolder<BlockFamily> nextFamily : BlockFamilies.findQuickSwitch(lastItem, player.isCreative())) {
					if (!iteratedFamilies.add(nextFamily.value())) {
						continue;
					}
					for (Item nextItem : nextFamily.value().items().toList()) {
						if (!iteratedItems.add(nextItem)) {
							continue;
						}
						float convertRatio = BlockFamilies.getConvertRatio(nextItem);
						CConvertItemPacket.Entry entry = new CConvertItemPacket.Entry(parentEntry.ratio() * ratio / convertRatio);
						entry.steps().addAll(parentEntry.steps());
						entry.steps().add(Pair.of(nextFamily, nextItem));
						if (!addedItems.contains(nextItem)) {
							group.entries().add(entry);
							addedItems.add(nextItem);
						}
						if (entry.steps().size() < CConvertItemPacket.MAX_STEPS && nextFamily.value().switchAttrs().cascading()) {
							unresolved.add(entry);
						}
					}
				}
			}
			if (!group.entries().isEmpty()) {
				groups.add(group);
			}
		}
		if (player.isCreative()) {
			return groups;
		}
		Predicate<CConvertItemPacket.Entry> filter = entry -> {
			if (entry.ratio() >= 1) {
				return false;
			}
			return !player.getInventory().hasAnyMatching($ -> $.is(entry.item()));
		};
		int count = 0;
		for (CConvertItemPacket.Group group : groups) {
			group.entries().removeIf(filter);
			count += group.entries().size();
		}
		if (count <= 1) {
			return List.of();
		}
		groups.removeIf(group -> group.entries().isEmpty());
		return groups;
	}

	public static boolean startDestroyBlock(BlockPos pos, Direction face) {
		LocalPlayer player = ensureBuilderMode();
		if (player == null) {
			return false;
		}
//		BlockState blockState = player.level().getBlockState(pos);
		return true;
	}

	public static boolean performUseItemOn(InteractionHand hand, BlockHitResult hitResult) {
		LocalPlayer player = ensureBuilderMode();
		if (player == null) {
			return false;
		}
		if (hand == InteractionHand.OFF_HAND) {
			return false;
		}
		BuilderModePreview preview = getPreviewRenderer();
		KHolder<BuilderRule> rule = preview.rule;
		BlockPos pos = preview.pos;
		List<BlockPos> positions = preview.positions;
		if (rule == null || positions.isEmpty() || !hitResult.getBlockPos().equals(pos)) {
			return true;
		}
		CApplyBuilderRulePacket.send(new UseOnContext(player, hand, hitResult), rule, positions);
		return true;
	}

	private static LocalPlayer ensureBuilderMode() {
		if (!isBuilderModeOn()) {
			return null;
		}
		return Minecraft.getInstance().player;
	}

	public static void renderDebugText(List<String> left, List<String> right) {
		if (!isBuilderModeOn() || Minecraft.getInstance().options.renderDebug) {
			return;
		}
		left.add("Builder Mode is on, long press %s to toggle".formatted(CustomizationClient.buildersButtonKey.getTranslatedKeyMessage()
				.getString()));
	}

	public static boolean cancelRenderHighlight() {
		return !getPreviewRenderer().positions.isEmpty();
	}
}

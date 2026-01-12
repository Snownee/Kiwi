package snownee.kiwi.customization.builder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.Window;
import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.customization.network.CConvertItemPacket;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.util.LerpedFloat;
import snownee.kiwi.util.MultilineTooltip;
import snownee.kiwi.util.client.SmartKey;

public class ConvertScreen extends Screen {
	private static @Nullable ConvertScreen lingeringScreen;
	private final boolean inContainer;
	private final boolean inCreativeContainer;
	@Nullable
	private final Slot slot;
	private final int slotIndex;
	private final Collection<CConvertItemPacket.Group> groups;
	private final LerpedFloat openProgress = LerpedFloat.linear();
	private PanelLayout layout;
	private final Vector2i originalMousePos;
	private final ItemStack sourceItem;
	private ClientTooltipPositioner forcedTooltipPositioner;
	private final Set<Item> chosenItems = Sets.newIdentityHashSet();
	private @Nullable AbstractWidget lastFocused;

	private static Vector2i getMousePos() {
		Minecraft mc = Minecraft.getInstance();
		MouseHandler mouseHandler = mc.mouseHandler;
		return new Vector2i((int) mouseHandler.xpos(), (int) mouseHandler.ypos());
	}

	public ConvertScreen(@Nullable Screen parent, @Nullable Slot slot, int slotIndex, List<CConvertItemPacket.Group> groups) {
		super(Component.translatable("gui.kiwi.builder.convert"));
		this.slot = slot;
		this.slotIndex = slotIndex;
		this.groups = groups;
		inContainer = parent instanceof AbstractContainerScreen;
		inCreativeContainer = inContainer && parent instanceof CreativeModeInventoryScreen;
		originalMousePos = getMousePos();
		openProgress.setValue(0.2f);
		openProgress.chase(1, 0.8, LerpedFloat.Chaser.EXP);
		sourceItem = getSourceItem();
		chosenItems.add(sourceItem.getItem());
	}

	private ItemStack getSourceItem() {
		if (slot != null) {
			return slot.getItem();
		}
		Inventory inventory = Objects.requireNonNull(Minecraft.getInstance().player).getInventory();
		return inventory.getItem(slotIndex);
	}

	@Override
	protected void init() {
		lastFocused = null;
		layout = new PanelLayout(2);
		int step = inContainer ? 19 : 21;
		int xStart = 0;
		int yStart = 0;
		int curX = xStart;
		int curY = yStart;
		Set<CConvertItemPacket.Entry> accepted = Sets.newHashSet();
		LocalPlayer player = Objects.requireNonNull(minecraft.player);
		for (CConvertItemPacket.Group group : groups) {
			accepted.addAll(group.entries());
		}
		int itemsPerLine = accepted.size() > 30 ? 11 : 4;
		for (CConvertItemPacket.Group group : groups) {
			for (CConvertItemPacket.Entry entry : group.entries()) {
				if (!accepted.contains(entry)) {
					continue;
				}
				ItemStack itemStack = new ItemStack(entry.item());
				ItemButton button = (ItemButton) ItemButton
						.builder(itemStack, inContainer, btn -> shortPress((ItemButton) btn, entry))
						.bounds(curX, curY, 21, 21)
						.build();

				button.onPress = btn -> {
					if (btn.pressTime() >= 10) {
						longPress(btn, entry);
					}
				};
				button.onRelease = _ -> {
					if (!SmartKey.hasControlDown()) {
						onClose();
					}
				};

				button.setAlpha(inContainer ? 0.2f : 0.8f);
				List<Component> tooltip;
				if (Platform.isProduction()) {
					tooltip = List.of(itemStack.getHoverName());
				} else {
					String steps = String.join(
							" -> ",
							entry.steps().stream().map(Pair::getFirst).map(Objects::toString).toList());
					tooltip = List.of(itemStack.getHoverName(), Component.literal(steps).withStyle(ChatFormatting.GRAY));
				}
				button.setTooltip(MultilineTooltip.create(tooltip));
				if (lastFocused == null && itemStack.is(sourceItem.getItem())) {
					lastFocused = button;
				}
				layout.addWidget(button);
				curX += step;
				if (curX >= xStart + itemsPerLine * step) {
					curX = xStart;
					curY += step;
				}
			}
		}
		int x;
		int y;
		Vector2f anchor;
		if (inContainer) {
			x = width / 2;
			y = height / 2;
			anchor = new Vector2f(0.5f, 0.5f);
		} else {
			if (slotIndex == Inventory.SLOT_OFFHAND) {
				HumanoidArm humanoidarm = player.getMainArm().getOpposite();
				if (humanoidarm == HumanoidArm.LEFT) {
					x = width / 2 - 91 - 29 + 11;
				} else {
					x = width / 2 + 91 + 17;
				}
			} else {
				x = width / 2 - 91 + 11 + player.getInventory().getSelectedSlot() * 20;
			}
			y = height - 24;
			anchor = new Vector2f(0.5f, 1f);
		}
		layout.bind(this, new Vector2i(x, y), anchor);
		if (lastFocused != null) {
			moveMouseOn(lastFocused);
		}
		Rect2i bounds = layout.bounds();
		ScreenRectangle rect = new ScreenRectangle(bounds.getX() - 2, bounds.getY() - 2, 10000, 10000);
		forcedTooltipPositioner = new BelowOrAboveWidgetTooltipPositioner(rect);
	}

	private void moveMouseOn(AbstractWidget button) {
		setFocused(button);
		Window window = Objects.requireNonNull(minecraft.getWindow());
		double scale = window.getGuiScale();
		GLFW.glfwSetCursorPos(window.handle(), (button.getX() + 15) * scale, (button.getY() + 15) * scale);
	}

	private void longPress(ItemButton button, CConvertItemPacket.Entry entry) {
		boolean convertOne = SmartKey.hasControlDown();
		if (convertOne) {
			shortPress(button, entry);
		} else if ((!inContainer || Objects.requireNonNull(minecraft.player).containerMenu instanceof InventoryMenu) &&
				button.pressTime() >= 15) {
			Item from = getSourceItem().getItem();
			KPacketSender.sendToServer(new CConvertItemPacket(
					false,
					slotIndex,
					entry,
					from,
					CConvertItemPacket.Action.CONVERT_FAMILY));
			onClose();
		}
	}

	private void shortPress(ItemButton button, CConvertItemPacket.Entry entry) {
		LocalPlayer player = Objects.requireNonNull(minecraft.player);
		boolean creative = player.isCreative();
		ItemStack sourceItem = getSourceItem();
		boolean convertOne = SmartKey.hasControlDown();
		if (convertOne) {
			if (!creative && sourceItem.getCount() <= 1) {
				onClose();
			}
		}
		Item from = sourceItem.getItem();
		Item to = button.item().getItem();
		if (!(creative && convertOne) && from == to) {
			return;
		}
		chosenItems.add(to);
		if (inCreativeContainer && convertOne) {
			// magic number time
			KPacketSender.sendToServer(new CConvertItemPacket(false, -500, entry, from, CConvertItemPacket.Action.CONVERT_ONE));
		} else if (inCreativeContainer) {
			Objects.requireNonNull(slot);
			ItemStack newItem = to.getDefaultInstance();
			newItem.setCount(slot.getItem().getCount());
			newItem.setPopTime(Inventory.POP_TIME_DURATION);
			slot.setByPlayer(newItem);
			NonNullList<Slot> slots = player.inventoryMenu.slots;
			for (int i = 0; i < slots.size(); i++) {
				if (slots.get(i).getItem() == newItem) {
					Objects.requireNonNull(minecraft.gameMode).handleCreativeModeItemAdd(newItem, i);
					CConvertItemPacket.Handler.playPickupSound(player);
					break;
				}
			}
		} else {
			KPacketSender.sendToServer(new CConvertItemPacket(
					inContainer,
					slotIndex,
					entry,
					from,
					convertOne ? CConvertItemPacket.Action.CONVERT_ONE : CConvertItemPacket.Action.CONVERT_ALL));
		}
	}

	@Override
	public void tick() {
		openProgress.tickChaser();
		if (!isClosing() && !chosenItems.contains(getSourceItem().getItem())) {
			onClose();
		}
	}

	@Override
	public void setFocused(@Nullable GuiEventListener listener) {
		if (listener instanceof ItemButton button) {
			lastFocused = button;
		}
		super.setFocused(listener);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (event.button() == 0) {
			Rect2i bounds = layout.bounds();
			Rect2i tolerance = new Rect2i(bounds.getX() - 10, bounds.getY() - 10, bounds.getWidth() + 20, bounds.getHeight() + 20);
			if (!tolerance.contains((int) event.x(), (int) event.y())) {
				onClose();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY == 0) {
			return false;
		}
		int index = -1;
		List<AbstractWidget> widgets = layout.widgets();
		if (widgets.isEmpty()) {
			return false;
		}
		if (lastFocused != null) {
			index = widgets.indexOf(lastFocused);
		}
		if (index == -1 && scrollY > 0) {
			index = widgets.size();
		}
		index += scrollY > 0 ? -1 : 1;
		if (index < 0) {
			index = widgets.size() - 1;
		} else if (index >= widgets.size()) {
			index = 0;
		}
		lastFocused = widgets.get(index);
		moveMouseOn(lastFocused);
		return true;
	}

	@Override
	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		Objects.requireNonNull(minecraft);
		Matrix3x2fStack pose = pGuiGraphics.pose();
		layout.update();
		Vector2i pos = layout.getAnchoredPos();
		float openValue = openProgress.getValue(pPartialTick);
		pose.pushMatrix();
		pose.translate(pos.x, pos.y);
		pose.scale(openValue);
		pose.translate(-pos.x, -pos.y);
		if (inContainer) {
			Rect2i bounds = layout.bounds();
			pGuiGraphics.blitSprite(
					RenderPipelines.GUI_TEXTURED,
					Identifier.withDefaultNamespace("recipe_book/overlay_recipe"),
					bounds.getX() - 2,
					bounds.getY() - 2,
					bounds.getWidth() + 3,
					bounds.getHeight() + 3);
		}
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
		pose.popMatrix();
	}

	@Override
	public void renderBackground(GuiGraphics p_283688_, int p_296369_, int p_296477_, float p_294317_) {
		// NO-OP
	}

//	@Override
//	public void setTooltipForNextRenderPass(List<FormattedCharSequence> list, ClientTooltipPositioner tooltipPositioner, boolean force) {
//		float openValue = openProgress.getValue(Objects.requireNonNull(minecraft)./*getPartialTick()*/ getTimer()
//				.getGameTimeDeltaPartialTick(true));
//		if (openValue > 0.95f) {
//			super.setTooltipForNextRenderPass(list, forcedTooltipPositioner, force);
//		}
//	}

	@Override
	public void onClose() {
		openProgress.chase(0, 0.8, LerpedFloat.Chaser.EXP);
		lingeringScreen = this;
		super.onClose();
		if (inContainer) {
			GLFW.glfwSetCursorPos(minecraft.getWindow().handle(), originalMousePos.x, originalMousePos.y);
		}
	}

	public boolean isClosing() {
		return openProgress.getChaseTarget() == 0;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public static void renderLingering(GuiGraphics pGuiGraphics) {
		if (lingeringScreen == null) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen != null || mc.getOverlay() != null || lingeringScreen.openProgress.settled()) {
			lingeringScreen = null;
			return;
		}
		lingeringScreen.render(
				pGuiGraphics,
				Integer.MAX_VALUE,
				Integer.MAX_VALUE,
				mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));
	}

	public static void tickLingering() {
		if (lingeringScreen != null) {
			lingeringScreen.tick();
		}
	}

}

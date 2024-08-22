package snownee.kiwi.customization.builder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.customization.network.CConvertItemPacket;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.util.KHolder;
import snownee.kiwi.util.LerpedFloat;
import snownee.kiwi.util.MultilineTooltip;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public class ConvertScreen extends Screen {
	private static ConvertScreen lingeringScreen;
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
		inCreativeContainer = parent instanceof CreativeModeInventoryScreen;
		originalMousePos = getMousePos();
		openProgress.setValue(0.2f);
		openProgress.chase(1, 0.8, LerpedFloat.Chaser.EXP);
		sourceItem = getSourceItem();
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
		layout = new PanelLayout(2);
		int step = inContainer ? 19 : 21;
		int xStart = 0;
		int yStart = 0;
		int curX = xStart;
		int curY = yStart;
		Set<CConvertItemPacket.Entry> accepted = Sets.newHashSet();
		LocalPlayer player = Objects.requireNonNull(getMinecraft().player);
		for (CConvertItemPacket.Group group : groups) {
			accepted.addAll(group.entries());
		}
		int itemsPerLine = accepted.size() > 30 ? 11 : 4;
		Button cursorOn = null;
		for (CConvertItemPacket.Group group : groups) {
			for (CConvertItemPacket.Entry entry : group.entries()) {
				if (!accepted.contains(entry)) {
					continue;
				}
				ItemStack itemStack = new ItemStack(entry.item());
				Button button = ItemButton.builder(itemStack, inContainer, btn -> {
					Item from = sourceItem.getItem();
					Item to = ((ItemButton) btn).getItem().getItem();
					if (from == to) {
						onClose();
						return;
					}
					boolean convertOne = hasControlDown();
					LocalPlayer player0 = Objects.requireNonNull(getMinecraft().player);
					if (inCreativeContainer && convertOne) {
						// magic number time
						KPacketSender.sendToServer(new CConvertItemPacket(false, -500, entry, from, true));
					} else if (inCreativeContainer) {
						Objects.requireNonNull(slot);
						ItemStack newItem = to.getDefaultInstance();
						newItem.setCount(slot.getItem().getCount());
						newItem.setPopTime(5);
						slot.setByPlayer(newItem);
						NonNullList<Slot> slots = player0.inventoryMenu.slots;
						for (int i = 0; i < slots.size(); i++) {
							if (slots.get(i).getItem() == newItem) {
								Objects.requireNonNull(getMinecraft().gameMode).handleCreativeModeItemAdd(newItem, i);
								CConvertItemPacket.Handler.playPickupSound(player0);
								break;
							}
						}
					} else {
						KPacketSender.sendToServer(new CConvertItemPacket(inContainer, slotIndex, entry, from, convertOne));
					}
					if (convertOne) {
						if (player0.isCreative() || sourceItem.getCount() > 1) {
							return;
						}
					}
					if (inContainer) {
						GLFW.glfwSetCursorPos(getMinecraft().getWindow().getWindow(), originalMousePos.x, originalMousePos.y);
					}
					onClose();
				}).bounds(curX, curY, 21, 21).build();
				button.setAlpha(inContainer ? 0.2f : 0.8f);
				List<Component> tooltip;
				if (Platform.isProduction()) {
					tooltip = List.of(itemStack.getHoverName());
				} else {
					String steps = String.join(
							" -> ",
							entry.steps().stream().map(Pair::getFirst).map(KHolder::key).map(Objects::toString).toList());
					tooltip = List.of(itemStack.getHoverName(), Component.literal(steps).withStyle(ChatFormatting.GRAY));
				}
				button.setTooltip(MultilineTooltip.create(tooltip));
				if (cursorOn == null && itemStack.is(sourceItem.getItem())) {
					cursorOn = button;
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
				x = width / 2 - 91 + 11 + player.getInventory().selected * 20;
			}
			y = height - 24;
			anchor = new Vector2f(0.5f, 1f);
		}
		layout.bind(this, new Vector2i(x, y), anchor);
		if (cursorOn != null) {
			Window window = getMinecraft().getWindow();
			double scale = window.getGuiScale();
			GLFW.glfwSetCursorPos(window.getWindow(), (cursorOn.getX() + 15) * scale, (cursorOn.getY() + 15) * scale);
		}
		Rect2i bounds = layout.bounds();
		ScreenRectangle rect = new ScreenRectangle(bounds.getX() - 2, bounds.getY() - 2, 10000, 10000);
		forcedTooltipPositioner = new BelowOrAboveWidgetTooltipPositioner(rect);
	}

	@Override
	public void tick() {
		openProgress.tickChaser();
		if (!isClosing() && !ItemStack.isSameItemSameComponents(sourceItem, getSourceItem())) {
			onClose();
		}
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
			return true;
		}
		if (pButton == 0) {
			Rect2i bounds = layout.bounds();
			Rect2i tolerance = new Rect2i(bounds.getX() - 10, bounds.getY() - 10, bounds.getWidth() + 20, bounds.getHeight() + 20);
			if (!tolerance.contains((int) pMouseX, (int) pMouseY)) {
				onClose();
				return true;
			}
		}
		return false;
	}

	@Override
	public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		Objects.requireNonNull(minecraft);
		PoseStack pose = pGuiGraphics.pose();
		layout.update();
		Vector2i pos = layout.getAnchoredPos();
		float openValue = openProgress.getValue(pPartialTick);
		pose.pushPose();
		pose.translate(pos.x, pos.y, 0);
		pose.scale(openValue, openValue, openValue);
		pose.translate(-pos.x, -pos.y, 0);
		if (inContainer) {
			Rect2i bounds = layout.bounds();
			pGuiGraphics.blitSprite(
					ResourceLocation.withDefaultNamespace("recipe_book/overlay_recipe"),
					bounds.getX() - 2,
					bounds.getY() - 2,
					0,
					bounds.getWidth() + 3,
					bounds.getHeight() + 3);
		}
		super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
		pose.popPose();
	}

	@Override
	public void renderBackground(GuiGraphics p_283688_, int p_296369_, int p_296477_, float p_294317_) {
		// NO-OP
	}

	@Override
	public void setTooltipForNextRenderPass(List<FormattedCharSequence> list, ClientTooltipPositioner tooltipPositioner, boolean force) {
		float openValue = openProgress.getValue(Objects.requireNonNull(minecraft)./*getPartialTick()*/ getTimer()
				.getGameTimeDeltaPartialTick(true));
		if (openValue > 0.95f) {
			super.setTooltipForNextRenderPass(list, forcedTooltipPositioner, force);
		}
	}

	@Override
	public void onClose() {
		openProgress.chase(0, 0.8, LerpedFloat.Chaser.EXP);
		lingeringScreen = this;
		super.onClose();
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
				mc.getTimer().getGameTimeDeltaPartialTick(true));
	}

	public static void tickLingering() {
		if (lingeringScreen != null) {
			lingeringScreen.tick();
		}
	}

}

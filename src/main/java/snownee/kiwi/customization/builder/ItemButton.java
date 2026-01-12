package snownee.kiwi.customization.builder;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class ItemButton extends Button {
	private final ItemStack itemStack;
	private final boolean inContainer;
	private float hoverProgress;
	private float pressTime = -1;
	public @Nullable Consumer<ItemButton> onPress;
	public @Nullable Consumer<ItemButton> onRelease;
	public @Nullable Tooltip rawTooltip; // we dont want the delay

	protected ItemButton(
			int x,
			int y,
			int width,
			int height,
			Component component,
			OnPress onPress,
			CreateNarration createNarration,
			ItemStack itemStack,
			boolean inContainer) {
		super(x, y, width, height, component, onPress, createNarration);
		this.itemStack = itemStack;
		this.inContainer = inContainer;
	}


	public static Builder builder(ItemStack itemStack, boolean inContainer, OnPress pOnPress) {
		return new Builder(itemStack, inContainer, pOnPress);
	}

	@Override
	public void setTooltip(@Nullable Tooltip tooltip) {
		rawTooltip = tooltip;
	}

	public ItemStack item() {
		return itemStack;
	}

	public int pressTime() {
		return (int) pressTime;
	}

	public void unpress() {
		pressTime = -1;
	}

	@Override
	protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		if (rawTooltip != null && isHovered() || isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard()) {
			Screen screen = Minecraft.getInstance().screen;
			if (screen != null) {
				pGuiGraphics.setTooltipForNextFrame(rawTooltip.toCharSequence(Minecraft.getInstance()), pMouseX, pMouseY);
			}
		}
		if (pressTime >= 0) {
			int i = (int) pressTime;
			pressTime += pPartialTick;
			if (onPress != null && i != (int) pressTime) {
				onPress.accept(this);
			}
		}
		int x = getX();
		int y = getY();
		int width = getWidth() - 1;
		int height = getHeight() - 1;
		if (inContainer) {
			pGuiGraphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x222222 | (int) (alpha * 0xFF) << 24);
		} else {
			pGuiGraphics.fill(x, y, x + width, y + height, 0x222222 | (int) (alpha * 0xFF) << 24);
		}
		hoverProgress += isHoveredOrFocused() ? pPartialTick * .2f : -pPartialTick * .2f;
		hoverProgress = Mth.clamp(hoverProgress, inContainer ? 0 : .4f, 1);
		int lineColor = 0xFFFFFF | (int) (hoverProgress * 0xFF) << 24;
		pGuiGraphics.fill(x, y, x + 1, y + height, lineColor);
		pGuiGraphics.fill(x + width - 1, y, x + width, y + height, lineColor);
		pGuiGraphics.fill(x + 1, y, x + width - 1, y + 1, lineColor);
		pGuiGraphics.fill(x + 1, y + height - 1, x + width - 1, y + height, lineColor);
		pGuiGraphics.renderItem(itemStack, x + 2, y + 2);
	}

	@Override
	public void onPress() {
		super.onPress();
		pressTime = 0;
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		onRelease();
	}

	@Override
	public boolean keyReleased(int button, int p_94751_, int p_94752_) {
		if (!this.active || !this.visible) {
			return false;
		}
		if (CommonInputs.selected(button)) {
			onRelease();
			return true;
		}
		return false;
	}

	public void onRelease() {
		if (onRelease != null && pressTime >= 0) {
			onRelease.accept(this);
		}
		pressTime = -1;
	}

	public static class Builder extends Button.Builder {
		private final ItemStack itemStack;
		private final boolean inContainer;

		protected Builder(ItemStack itemStack, boolean inContainer, OnPress pOnPress) {
			super(itemStack.getHoverName(), pOnPress);
			this.itemStack = itemStack;
			this.inContainer = inContainer;
		}

		@Override
		public ItemButton build() {
			var button = super.build();
			return new ItemButton(
					button.getX(),
					button.getY(),
					button.getWidth(),
					button.getHeight(),
					button.getMessage(),
					button.onPress,
					button.createNarration,
					itemStack,
					inContainer);
		}
	}
}

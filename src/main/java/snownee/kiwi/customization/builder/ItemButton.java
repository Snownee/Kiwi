package snownee.kiwi.customization.builder;

import java.time.Duration;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class ItemButton extends Button {
	private final ItemStack itemStack;
	private final boolean inContainer;
	private float hoverProgress;
	private float pressTime = -1;
	public @Nullable Consumer<ItemButton> onPress;
	public @Nullable Consumer<ItemButton> onRelease;

	protected ItemButton(Builder builder) {
		super(builder);
		itemStack = builder.itemStack;
		inContainer = builder.inContainer;
	}

	public static Builder builder(ItemStack itemStack, boolean inContainer, Button.OnPress pOnPress) {
		return new Builder(itemStack, inContainer, pOnPress);
	}

	@Override
	public void setTooltipDelay(Duration delay) {
		// Needn't delay
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
	protected void extractContents(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
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
			graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x222222 | (int) (alpha * 0xFF) << 24);
		} else {
			graphics.fill(x, y, x + width, y + height, 0x222222 | (int) (alpha * 0xFF) << 24);
		}
		hoverProgress += isHoveredOrFocused() ? pPartialTick * .2f : -pPartialTick * .2f;
		hoverProgress = Mth.clamp(hoverProgress, inContainer ? 0 : .4f, 1);
		int lineColor = 0xFFFFFF | (int) (hoverProgress * 0xFF) << 24;
		graphics.fill(x, y, x + 1, y + height, lineColor);
		graphics.fill(x + width - 1, y, x + width, y + height, lineColor);
		graphics.fill(x + 1, y, x + width - 1, y + 1, lineColor);
		graphics.fill(x + 1, y + height - 1, x + width - 1, y + height, lineColor);
		graphics.item(itemStack, x + 2, y + 2);
	}

	@Override
	public void onPress(InputWithModifiers input) {
		super.onPress(input);
		pressTime = 0;
	}

	@Override
	public void onRelease(MouseButtonEvent event) {
		onRelease();
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!this.active || !this.visible) {
			return false;
		}
		if (event.isSelection()) {
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
			return new ItemButton(this);
		}
	}
}

package snownee.kiwi.customization.builder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public class ItemButton extends Button {
	private final ItemStack itemStack;
	private final boolean inContainer;
	private ClientTooltipPositioner tooltipPositioner;
	private float hoverProgress;

	protected ItemButton(Builder builder) {
		super(builder);
		itemStack = builder.itemStack;
		inContainer = builder.inContainer;
	}

	public static Builder builder(ItemStack itemStack, boolean inContainer, Button.OnPress pOnPress) {
		return new Builder(itemStack, inContainer, pOnPress);
	}

	public ItemStack getItem() {
		return itemStack;
	}

	@Override
	protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
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

	public void setTooltipPositioner(ClientTooltipPositioner tooltipPositioner) {
		this.tooltipPositioner = tooltipPositioner;
	}

	@Override
	protected ClientTooltipPositioner createTooltipPositioner() {
		return tooltipPositioner != null ? tooltipPositioner : super.createTooltipPositioner();
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

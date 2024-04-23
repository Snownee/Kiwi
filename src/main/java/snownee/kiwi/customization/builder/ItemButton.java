package snownee.kiwi.customization.builder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public class ItemButton extends Button {
	private final ItemStack itemStack;
	private float hoverProgress;

	protected ItemButton(Builder builder) {
		super(builder);
		itemStack = builder.itemStack;
	}

	public static Builder builder(ItemStack itemStack, Button.OnPress pOnPress) {
		return new Builder(itemStack, pOnPress);
	}

	public ItemStack getItem() {
		return itemStack;
	}

	@Override
	protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		int x = getX();
		int y = getY();
		pGuiGraphics.fill(x, y, x + width, y + height, 0x222222 | (int) (alpha * 0xFF) << 24);
		hoverProgress += isHoveredOrFocused() ? pPartialTick * .2f : -pPartialTick * .2f;
		hoverProgress = Mth.clamp(hoverProgress, .4f, 1);
		int lineColor = 0xFFFFFF | (int) (hoverProgress * 0xFF) << 24;
		pGuiGraphics.fill(x, y, x + 1, y + height, lineColor);
		pGuiGraphics.fill(x + width - 1, y, x + width, y + height, lineColor);
		pGuiGraphics.fill(x + 1, y, x + width - 1, y + 1, lineColor);
		pGuiGraphics.fill(x + 1, y + height - 1, x + width - 1, y + height, lineColor);
		pGuiGraphics.renderItem(itemStack, x + 2, y + 2);
	}

	public static class Builder extends Button.Builder {
		private final ItemStack itemStack;

		protected Builder(ItemStack itemStack, OnPress pOnPress) {
			super(itemStack.getHoverName(), pOnPress);
			this.itemStack = itemStack;
		}

		@Override
		public ItemButton build() {
			return new ItemButton(this);
		}
	}
}

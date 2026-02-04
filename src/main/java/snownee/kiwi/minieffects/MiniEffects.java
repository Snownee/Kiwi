package snownee.kiwi.minieffects;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.Kiwi;
import snownee.kiwi.loader.Platform;

public class MiniEffects {
	private static final boolean hasREI = Platform.isModLoaded("roughlyenoughitems");
	private static final Identifier EFFECT_BUTTON_SPRITE = Kiwi.id("minieffects");

	public static boolean isLeftSide() {
//		if (hasREI && ConfigObject.getInstance().isLeftSideMobEffects()) {
//			return true;
//		}
		return MiniEffectsConfig.effectsOnLeft;
	}

	public static boolean allowClick(EffectRenderingScreen screen, MouseButtonEvent event) {
		KiwiEffectsInInventory effects = (KiwiEffectsInInventory) screen.kiwi$effects();
		ScreenRectangle buttonArea = effects.kiwi$buttonArea();
		if (!effects.kiwi$isExpanded() && buttonArea != null && buttonArea.containsPoint((int) event.x(), (int) event.y())) {
			effects.kiwi$setExpanded(true);
			return false;
		}
		return true;
	}

	public static boolean afterClick(EffectRenderingScreen screen, MouseButtonEvent event) {
		KiwiEffectsInInventory effects = (KiwiEffectsInInventory) screen.kiwi$effects();
		if (effects.kiwi$isExpanded()) {
			effects.kiwi$setExpanded(false);
			return false;
		}
		return true;
	}

	public static void render(GuiGraphics graphics, Font font, ScreenRectangle area, ItemStack iconItem, int effects, int bad) {
		int x = area.left();
		int y = area.top();
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EFFECT_BUTTON_SPRITE, x, y, area.width(), area.height());

		var pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(x + 1, y + 2);
		pose.scale(0.5f);
		graphics.renderFakeItem(iconItem, 0, 0);
		if (effects > 0) {
			String s = Integer.toString(effects);
			graphics.drawString(
					font,
					s,
					18 - font.width(s),
					8,
					bad == 0 ? 0xFFFFFFFF : 0xFFFF5555
			);
		}
		pose.popMatrix();
	}
}

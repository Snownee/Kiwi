package snownee.kiwi.customization.builder;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector2i;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;

public class PanelLayout {
	private final Vector2i pos = new Vector2i();
	private final Vector2i lastPos = new Vector2i();
	//	private final Vector2f anchor = new Vector2f(0, 0);
	private final Rect2i bounds = new Rect2i(0, 0, 0, 0);
	private final int padding;
	private final List<AbstractWidget> widgets = Lists.newArrayList();

	public PanelLayout(int padding) {
		this.padding = padding;
	}

	public void addWidget(AbstractWidget widget) {
		widgets.add(widget);
	}

	public void bind(Screen screen, Vector2i pos, Vector2f anchor) {
		Preconditions.checkArgument(!widgets.isEmpty());
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		for (AbstractWidget widget : widgets) {
			//don't know why, but I can't mixin-access Screen.addRenderableWidget()
			screen.renderables.add(widget);
			screen.children.add(widget);
			screen.narratables.add(widget);

			minX = Math.min(minX, widget.getX());
			minY = Math.min(minY, widget.getY());
			maxX = Math.max(maxX, widget.getX() + widget.getWidth());
			maxY = Math.max(maxY, widget.getY() + widget.getHeight());
		}
		bounds.setWidth(maxX - minX + padding * 2);
		bounds.setHeight(maxY - minY + padding * 2);
		bounds.setX(bounds.getX() - padding);
		bounds.setY(bounds.getY() - padding);
//		this.anchor.set(anchor);
		this.pos.set(pos);
		this.lastPos.set((int) (bounds.getWidth() * anchor.x) - padding, (int) (bounds.getHeight() * anchor.y) - padding);
		update();
	}

	public Vector2i getAnchoredPos() {
		return pos;
	}

	public void update() {
		int deltaX = pos.x - lastPos.x;
		int deltaY = pos.y - lastPos.y;
		if (deltaX == 0 && deltaY == 0) {
			return;
		}
		for (AbstractWidget widget : widgets) {
			widget.setPosition(widget.getX() + deltaX, widget.getY() + deltaY);
		}
		lastPos.set(pos);
		bounds.setX(bounds.getX() + deltaX);
		bounds.setY(bounds.getY() + deltaY);
	}

	public Rect2i getBounds() {
		return bounds;
	}
}

package snownee.kiwi.client.gui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.CompoundNBT;
import snownee.kiwi.client.gui.component.Component;

public class GuiControl implements IMessageHandler {
	protected final List<Component> components = new ArrayList<>();
	public Minecraft mc;
	public IMessageHandler messageHandler;
	public int offsetX;
	public int offsetY;
	public int width;
	public int height;

	public GuiControl(Minecraft mc, int width, int height, IMessageHandler messageHandler) {
		this.mc = mc;
		this.width = width;
		this.height = height;
		this.messageHandler = messageHandler;
	}

	public void addComponent(Component component) {
		boolean flag = true;
		for (int i = 0; i < components.size(); ++i) {
			Component c = components.get(i);
			if (component.getZLevel() >= c.getZLevel()) {
				components.add(i, component);
				flag = true;
				break;
			}
		}
		if (flag) {
			components.add(component);
		}
	}

	public boolean removeComponent(int index) {
		Component component = null;
		if (index >= 0 && index < components.size()) {
			component = components.remove(index);
		}
		if (component != null) {
			component.onDestroy();
		}
		return component != null;
	}

	public boolean removeComponent(Component component) {
		boolean flag = components.remove(component);
		if (flag) {
			component.onDestroy();
		}
		return flag;
	}

	public void removeAllComponents() {
		for (Component c : components) {
			c.onDestroy();
		}
		components.clear();
	}

	@Nullable
	public int getComponentSize(@Nullable Class<? extends Component> clazz) {
		if (clazz == null || clazz == Component.class) {
			return components.size();
		} else {
			return 0; //TODO
		}
	}

	@Nullable
	public Component getComponent(int index) {
		return components.get(index);
	}

	@Nullable
	public <T extends Component> T getComponent(Class<T> clazz) {
		for (Component component : components) {
			if (clazz.isInstance(component)) {
				return (T) component;
			}
		}
		return null;
	}

	public <T extends Component> List<T> getComponents(Class<T> clazz) {
		List<T> list = new ArrayList<>(Math.max(4, components.size() / 4));
		for (Component component : components) {
			if (clazz.isInstance(component)) {
				list.add((T) component);
			}
		}
		return list;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		for (Component c : components) {
			if (c.visible) {
				c.drawScreen(offsetX, offsetY, mouseX - offsetX, mouseY - offsetY, partialTicks);
			}
		}
	}

	public void keyTyped(char typedChar, int keyCode) {
		for (Component c : components) {
			if (c.visible) {
				c.keyTyped(typedChar, keyCode);
			}
		}
	}

	public void handleMouseInput(int mouseX, int mouseY) {
		for (Component c : components) {
			if (c.visible) {
				c.handleMouseInput(mouseX - offsetX, mouseY - offsetY);
			}
		}
	}

	@OverridingMethodsMustInvokeSuper
	public void onDestroy() {
		for (Component c : components) {
			c.onDestroy();
		}
		components.clear();
		this.mc = null;
		this.messageHandler = null;
	}

	@Override
	public int messageReceived(GuiControl control, Component component, int param1, int param2) {
		return this.messageHandler.messageReceived(control, component, param1, param2);
	}

	@Override
	public int messageReceived(GuiControl control, Component component, CompoundNBT data) {
		return this.messageHandler.messageReceived(control, component, data);
	}

	@Override
	public void setTooltip(GuiControl control, Component component, List<String> tooltip, FontRenderer fontRenderer) {
		this.messageHandler.setTooltip(control, component, tooltip, fontRenderer);
	}

}

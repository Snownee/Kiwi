//package snownee.kiwi.client.gui.component;
//
//import java.util.List;
//
//import javax.annotation.Nullable;
//import javax.annotation.OverridingMethodsMustInvokeSuper;
//
//import net.minecraft.client.gui.Font;
//import net.minecraft.nbt.CompoundTag;
//import snownee.kiwi.client.gui.GuiControl;
//
//public abstract class Component {
//	private int zLevel;
//	public int width;
//	public int height;
//	public int top;
//	public int left;
//	public boolean visible;
//	protected GuiControl parent;
//
//	public Component(GuiControl parent, int width, int height) {
//		this.parent = parent;
//		this.width = width;
//		this.height = height;
//		visible = true;
//	}
//
//	public int getZLevel() {
//		return zLevel;
//	}
//
//	public abstract void drawScreen(int offsetX, int offsetY, int relMouseX, int relMouseY, float partialTicks);
//
//	public void keyTyped(char typedChar, int keyCode) {
//	}
//
//	public void handleMouseInput(int relMouseX, int relMouseY) {
//	}
//
//	@OverridingMethodsMustInvokeSuper
//	public void onDestroy() {
//		parent = null;
//	}
//
//	public int sendMessage(int param1, int param2) {
//		return parent.messageHandler.messageReceived(parent, this, param1, param2);
//	}
//
//	public int sendMessage(CompoundTag data) {
//		return parent.messageHandler.messageReceived(parent, this, data);
//	}
//
//	public void setTooltip(List<String> tooltip, @Nullable Font fontRenderer) {
//		parent.messageHandler.setTooltip(parent, this, tooltip, fontRenderer);
//	}
//}

package snownee.kiwi.client.gui;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.CompoundNBT;
import snownee.kiwi.client.gui.component.Component;

public interface IMessageHandler {
	int messageReceived(GuiControl control, Component component, int param1, int param2);

	int messageReceived(GuiControl control, Component component, CompoundNBT data);

	void setTooltip(GuiControl control, Component component, List<String> tooltip, FontRenderer fontRenderer);
}

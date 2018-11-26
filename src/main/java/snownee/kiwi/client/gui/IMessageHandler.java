package snownee.kiwi.client.gui;

import net.minecraft.nbt.NBTTagCompound;
import snownee.kiwi.client.gui.component.Component;

public interface IMessageHandler
{
    int messageReceived(GuiControl control, Component component, int param1, int param2);

    int messageReceived(GuiControl control, Component component, NBTTagCompound data);
}

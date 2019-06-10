package snownee.kiwi;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

public class ModContext
{
    public ModContainer modContainer;
    public Supplier<?> extension;

    public ModContext(String modid)
    {
        if (modid == null)
        {
            Kiwi.logger.error("Cannot get name of kiwi module.");
            return;
        }
        try
        {
            this.modContainer = ModList.get().getModContainerById(modid).get();
            this.extension = (Supplier<?>) Kiwi.FIELD_EXTENSION.get(modContainer);
        }
        catch (NoSuchElementException | IllegalArgumentException | IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setActiveContainer()
    {
        ModLoadingContext.get().setActiveContainer(modContainer, extension.get());
    }
}

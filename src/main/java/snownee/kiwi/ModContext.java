package snownee.kiwi;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

public class ModContext {
    public ModContainer modContainer;
    public Supplier<?> extension;

    public ModContext(String modid) {
        Preconditions.checkNotNull(modid, "Cannot get name of kiwi module.");
        try {
            this.modContainer = ModList.get().getModContainerById(modid).get();
            this.extension = (Supplier<?>) Kiwi.FIELD_EXTENSION.get(modContainer);
        } catch (NoSuchElementException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setActiveContainer() {
        ModLoadingContext.get().setActiveContainer(modContainer, extension.get());
    }
}

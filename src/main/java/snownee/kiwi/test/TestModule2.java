package snownee.kiwi.test;

import net.minecraft.entity.EntityType;
import net.minecraft.tags.Tag;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;

@KiwiModule(name = "sub", dependencies = "forge;@test")
@KiwiModule.Optional(disabledByDefault = true)
public class TestModule2 extends AbstractModule {
    public static final Tag<EntityType<?>> BAT = entityTag(Kiwi.MODID, "bat");

    @Override
    protected void init(FMLCommonSetupEvent event) {
        System.out.println(1);
    }
}

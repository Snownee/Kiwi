package snownee.kiwi.test;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiModule;

@KiwiModule(name = "sub", dependencies = "forge;@test")
@KiwiModule.Optional(disabledByDefault = true)
public class TestModule2 extends AbstractModule {
    @Override
    protected void init(FMLCommonSetupEvent event) {
        System.out.println(1);
    }
}

package snownee.kiwi.test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.Name;

@KiwiModule(value = "test2", dependencies = "forge;@kiwi:test")
@KiwiModule.Optional(disabledByDefault = true)
public class TestModule2 extends AbstractModule {
	public static Named<EntityType<?>> BAT = entityTag(Kiwi.MODID, "bat");

	@Name("kiwi:test_item")
	public static TestItem FIRST_ITEM = new TestItem(itemProp().rarity(Rarity.EPIC)) {
		@Override
		public boolean isFoil(ItemStack stack) {
			return true;
		}
	};

	static {
		Kiwi.applyObjectHolder(ForgeRegistries.ITEMS, new ResourceLocation(Kiwi.MODID, "first_item"));
	}

	@Override
	protected void init(FMLCommonSetupEvent event) {
		System.out.println(1);
	}
}

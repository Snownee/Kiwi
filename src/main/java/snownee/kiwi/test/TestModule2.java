package snownee.kiwi.test;

import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.Name;

@KiwiModule(value = "test2", dependencies = "forge;@kiwi:test")
@KiwiModule.Optional(disabledByDefault = true)
public class TestModule2 extends AbstractModule {
	public static final INamedTag<EntityType<?>> BAT = entityTag(Kiwi.MODID, "bat");

	@Name("kiwi:test_item")
	public static final TestItem FIRST_ITEM = new TestItem(itemProp().rarity(Rarity.EPIC)) {
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

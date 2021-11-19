package snownee.kiwi.test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Name;
import snownee.kiwi.loader.event.InitEvent;

@KiwiModule(value = "test2", dependencies = "forge;@kiwi:test")
@KiwiModule.Optional(disabledByDefault = true)
public class TestModule2 extends AbstractModule {
	public static CreativeModeTab TAB = new CreativeModeTab("my_mod.items") {
		@Override
		public ItemStack makeIcon() {
			return new ItemStack(Items.DANDELION);
		}
	};

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
	protected void init(InitEvent event) {
		System.out.println(1);
	}
}

package snownee.kiwi.test;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.KiwiModule.Name;
import snownee.kiwi.loader.event.InitEvent;
import snownee.kiwi.util.VanillaActions;

@KiwiModule(modId = Kiwi.ID, value = "test2", dependencies = "minecraft;@kiwi:test")
@KiwiModule.Optional(defaultEnabled = false)
public class TestModule2 extends AbstractModule {
	public static final KiwiGO<CreativeModeTab> TAB = go(() -> itemCategory(
			ResourceLocation.fromNamespaceAndPath("my_mod", "items"),
			() -> new ItemStack(Items.DANDELION)).build());

	public static final TagKey<EntityType<?>> BAT = entityTag("bat");

	@Name("kiwi:test_item")
	public static final KiwiGO<TestItem> FIRST_ITEM = go(() -> new TestItem(itemProp().rarity(Rarity.EPIC)) {
		@Override
		public boolean isFoil(ItemStack stack) {
			return true;
		}
	});

	@Name("minecraft:dandelion")
	public static final KiwiGO<Item> DANDELION = ref(Registries.ITEM);

	@Override
	protected void init(InitEvent event) {
		event.enqueueWork(() -> {
			VanillaActions.registerAxeConversion(Blocks.DIAMOND_BLOCK, Blocks.REDSTONE_BLOCK);
			Kiwi.LOGGER.info("{}", DANDELION.get());
		});
	}
}

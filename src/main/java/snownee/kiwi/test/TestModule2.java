package snownee.kiwi.test;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
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

@KiwiModule(value = "test2")
@KiwiModule.Optional(defaultEnabled = false)
public class TestModule2 extends AbstractModule {
	public static final KiwiGO<CreativeModeTab> TAB = go(() -> itemCategory("my_mod", "items", () -> new ItemStack(Items.DANDELION)).build());

	public static final TagKey<EntityType<?>> BAT = entityTag(Kiwi.ID, "bat");

	@Name("kiwi:test_item")
	public static final KiwiGO<TestItem> FIRST_ITEM = go(() -> new TestItem(itemProp().rarity(Rarity.EPIC)) {
		@Override
		public boolean isFoil(ItemStack stack) {
			return true;
		}
	});

	@Override
	protected void init(InitEvent event) {
		event.enqueueWork(() -> VanillaActions.registerShovelConversion(Blocks.DIAMOND_BLOCK, Blocks.REDSTONE_BLOCK.defaultBlockState()));
	}
}

//package snownee.kiwi.data.loot;
//
//import java.util.List;
//
//import com.google.gson.JsonObject;
//
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.GsonHelper;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.world.storage.loot.LootContext;
//import net.minecraft.world.storage.loot.LootTable;
//import net.minecraft.world.storage.loot.conditions.ILootCondition;
//import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
//import net.minecraftforge.common.loot.LootModifier;
//
//@Deprecated
//public class AddLootTable extends LootModifier {
//
//    private final ResourceLocation lootTable;
//
//    public AddLootTable(ILootCondition[] conditionsIn, ResourceLocation lootTable) {
//        super(conditionsIn);
//        this.lootTable = lootTable;
//    }
//
//    private boolean running;
//
//    @Override
//    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
//        if (running) {
//            return generatedLoot;
//        }
//
//        running = true;
//        LootTable table = context.func_227502_a_(lootTable);
//        generatedLoot.addAll(table.generate(context));
//        running = false;
//        return generatedLoot;
//    }
//
//    public static class Serializer extends GlobalLootModifierSerializer<AddLootTable> {
//        @Override
//        public AddLootTable read(ResourceLocation location, JsonObject object, ILootCondition[] ailootcondition) {
//            ResourceLocation lootTable = new ResourceLocation(GsonHelper.getString(object, "loot_table"));
//            return new AddLootTable(ailootcondition, lootTable);
//        }
//    }
//
//}

//package snownee.kiwi.data.loot;
//
//import com.google.gson.JsonDeserializationContext;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonSerializationContext;
//
//import net.minecraft.util.JSONUtils;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.world.storage.loot.LootContext;
//import net.minecraft.world.storage.loot.LootTable;
//import net.minecraft.world.storage.loot.conditions.ILootCondition;
//import snownee.kiwi.Kiwi;
//
//public class HasLootTable implements ILootCondition {
//
//    private final ResourceLocation lootTable;
//
//    public HasLootTable(ResourceLocation lootTable) {
//        this.lootTable = lootTable;
//    }
//
//    @Override
//    public boolean test(LootContext context) {
//        LootTable table = context.func_227502_a_(lootTable);
//        if (table == LootTable.EMPTY_LOOT_TABLE) {
//            return false;
//        }
//        return true;
//    }
//
//    public static class Serializer extends AbstractSerializer<HasLootTable> {
//
//        public Serializer() {
//            super(new ResourceLocation(Kiwi.MODID, "has_loot_table"), HasLootTable.class);
//        }
//
//        @Override
//        public void serialize(JsonObject json, HasLootTable value, JsonSerializationContext context) {
//            json.addProperty("loot_table", value.lootTable.toString());
//        }
//
//        @Override
//        public HasLootTable deserialize(JsonObject json, JsonDeserializationContext context) {
//            return new HasLootTable(new ResourceLocation(JSONUtils.getString(json, "loot_table")));
//        }
//
//    }
//
//}

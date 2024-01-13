package snownee.kiwi.recipe.crafting;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import snownee.kiwi.block.def.BlockDefinition;
import snownee.kiwi.block.entity.RetextureBlockEntity;
import snownee.kiwi.data.DataModule;
import snownee.kiwi.recipe.FullBlockIngredient;

public class RetextureRecipe extends DynamicShapedRecipe {
	protected Char2ObjectMap<String[]> textureKeys;

	public RetextureRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification, boolean differentInputs, Map<Character, List<String>> textureKeys) {
		super(group, category, pattern, result, showNotification, differentInputs);
		this.textureKeys = new Char2ObjectArrayMap<>(textureKeys.size());
		for (Entry<Character, List<String>> e : textureKeys.entrySet()) {
			this.textureKeys.put(e.getKey().charValue(), e.getValue().toArray(new String[0]));
		}
	}

	public RetextureRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(CraftingContainer inv, int x, int y, int rx, int ry) {
		ItemStack stack = inv.getItem(x + y * inv.getWidth());
		return (getEmptyPredicate().test(stack) || FullBlockIngredient.isTextureBlock(stack)) && super.matches(inv, x, y, rx, ry);
	}

	@Override
	public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
		int[] pos = search(inv);
		if (pos == null) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = result.copy();
		Map<String, BlockDefinition> map = Maps.newHashMap();
		for (Char2ObjectMap.Entry<String[]> e : textureKeys.char2ObjectEntrySet()) {
			ItemStack item = item(e.getCharKey(), inv, pos);
			BlockDefinition def = BlockDefinition.fromItem(item, null);
			for (String k : e.getValue()) {
				map.put(k, def);
			}
		}
		RetextureBlockEntity.writeTextures(map, stack.getOrCreateTagElement("BlockEntityTag"));
		return stack;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return DataModule.RETEXTURE.get();
	}

	public static class Serializer extends DynamicShapedRecipe.Serializer<RetextureRecipe> {

		private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap(string -> {
			if (string.length() != 1) {
				return DataResult.error(() -> "Invalid key entry: '" + string + "' is an invalid symbol (must be 1 character only).");
			}
			if (" ".equals(string)) {
				return DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.");
			}
			return DataResult.success(Character.valueOf(string.charAt(0)));
		}, String::valueOf);
		public static final Codec<RetextureRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(DynamicShapedRecipe::getGroup),
				CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(DynamicShapedRecipe::category),
				ShapedRecipePattern.MAP_CODEC.forGetter(DynamicShapedRecipe::pattern),
				ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter(DynamicShapedRecipe::result),
				ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter(DynamicShapedRecipe::showNotification),
				Codec.BOOL.fieldOf("different_inputs").orElse(false).forGetter(RetextureRecipe::allowDifferentInputs),
				Codec.unboundedMap(SYMBOL_CODEC, Codec.list(Codec.STRING)).fieldOf("texture").forGetter(recipe -> {
					Map<Character, List<String>> map = Maps.newHashMap();
					for (Char2ObjectMap.Entry<String[]> e : recipe.textureKeys.char2ObjectEntrySet()) {
						map.put(e.getCharKey(), List.of(e.getValue()));
					}
					return map;
				})
		).apply(instance, RetextureRecipe::new));

		@Override
		public Codec<RetextureRecipe> codec() {
			return CODEC;
		}

		@Override
		public RetextureRecipe fromNetwork(FriendlyByteBuf pBuffer) {
			RetextureRecipe recipe = fromNetwork(RetextureRecipe::new, pBuffer);
			int size = pBuffer.readVarInt();
			recipe.textureKeys = new Char2ObjectArrayMap<>(size);
			for (int i = 0; i < size; i++) {
				char ch = pBuffer.readChar();
				int size2 = pBuffer.readVarInt();
				String[] arr = new String[size2];
				for (int j = 0; j < size2; j++) {
					arr[j] = pBuffer.readUtf(16);
				}
				recipe.textureKeys.put(ch, arr);
			}
			return recipe;
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, RetextureRecipe recipe) {
			buffer.writeEnum(recipe.category());
			super.toNetwork(buffer, recipe);
			buffer.writeVarInt(recipe.textureKeys.size());
			for (Char2ObjectMap.Entry<String[]> e : recipe.textureKeys.char2ObjectEntrySet()) {
				buffer.writeChar(e.getCharKey());
				buffer.writeVarInt(e.getValue().length);
				for (String s : e.getValue()) {
					buffer.writeUtf(s, 16);
				}
			}
		}

	}
}

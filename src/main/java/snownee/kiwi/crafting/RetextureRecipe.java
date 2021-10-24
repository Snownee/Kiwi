package snownee.kiwi.crafting;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistryEntry;
import snownee.kiwi.data.DataModule;
import snownee.kiwi.util.NBTHelper;

@Deprecated
public class RetextureRecipe extends DynamicShapedRecipe {
	private final String[] textureKeys;

	public RetextureRecipe(ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn, NonNullList<Ingredient> ingredients, ItemStack recipeOutputIn, String[] textureKeys) {
		super(idIn, groupIn, recipeWidthIn, recipeHeightIn, ingredients, recipeOutputIn);
		this.textureKeys = textureKeys;
	}

	@Override
	protected boolean checkMatch(CraftingContainer inv, int startX, int startY) {
		return checkMatchInternal(inv, startX, startY) != null;
	}

	private Map<String, ItemStack> checkMatchInternal(CraftingContainer inv, int startX, int startY) {
		Map<String, ItemStack> result = Maps.newHashMap();
		int i = 0;
		for (int y = startY; y < startY + getRecipeHeight(); ++y) {
			for (int x = startX; x < startX + getRecipeWidth(); ++x) {
				String key = textureKeys[i];
				if (!key.isEmpty()) {
					ItemStack slotStack = inv.getItem(x + y * inv.getWidth());
					Ingredient ingredient = getIngredients().get(x - startX + (y - startY) * getRecipeWidth());
					if (!(ingredient instanceof FullBlockIngredient) && !FullBlockIngredient.isTextureBlock(slotStack)) {
						return null;
					}
					ItemStack stack = result.getOrDefault(key, ItemStack.EMPTY);
					if (stack.isEmpty()) {
						result.put(key, slotStack);
					} else {
						if (!stack.sameItem(slotStack)) {
							return null;
						}
					}
				}
				if (!matches(inv, x, y, x - startX, y - startY)) {
					return null;
				}
				++i;
			}
		}
		return result;
	}

	@Override
	public ItemStack assemble(CraftingContainer inv) {
		int[] pos = getMatchPos(inv);
		if (pos == null) {
			return ItemStack.EMPTY;
		}
		Map<String, ItemStack> result = checkMatchInternal(inv, pos[0], pos[1]);
		if (result == null) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = getResultItem().copy();
		NBTHelper data = NBTHelper.of(stack.getOrCreateTagElement("BlockEntityTag"));
		for (Entry<String, ItemStack> e : result.entrySet()) {
			Item item = e.getValue().getItem();
			if (item instanceof BlockItem) {
				BlockState state = ((BlockItem) item).getBlock().defaultBlockState();
				for (String k : e.getKey().split(",")) {
					String texture = NBTHelper.of(e.getValue()).getString("BlockEntityTag.Textures." + k);
					if (texture == null) {
						texture = NbtUtils.writeBlockState(state).toString();
					}
					data.setString("Textures." + k, texture);
				}
			} else {
				return ItemStack.EMPTY;
			}
		}
		return stack;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return DataModule.RETEXTURE;
	}

	public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<RetextureRecipe> {
		@Override
		public RetextureRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			String group = GsonHelper.getAsString(json, "group", "");
			Map<String, Ingredient> ingredientMap = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
			String[] pattern = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
			int width = pattern[0].length();
			int height = pattern.length;
			NonNullList<Ingredient> nonnulllist = ShapedRecipe.dissolvePattern(pattern, ingredientMap, width, height);
			ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

			Map<String, String> texMap = Maps.newHashMap();
			for (Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(json, "texture").entrySet()) {
				if (entry.getKey().length() != 1) {
					throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
				}
				if (" ".equals(entry.getKey()) || ",".equals(entry.getKey())) {
					throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is a reserved symbol.");
				}
				if (entry.getValue().isJsonArray()) {
					/* off */
                    List<String> keys = Lists.newArrayList(entry.getValue().getAsJsonArray())
                            .stream()
                            .map(JsonElement::getAsString)
                            .collect(Collectors.toList());
                    /* on */
					texMap.put(entry.getKey(), StringUtils.join(keys, ','));
				} else {
					texMap.put(entry.getKey(), entry.getValue().getAsString());
				}
			}
			String[] keys = new String[width * height];
			Set<String> set = Sets.newHashSet(texMap.keySet());
			int i = 0;
			for (String element : pattern) {
				for (int j = 0; j < element.length(); ++j) {
					String s = String.valueOf(element.charAt(j));
					if (texMap.containsKey(s)) {
						keys[i] = texMap.get(s);
						set.remove(s);
					} else {
						keys[i] = "";
					}
					++i;
				}
			}
			if (!set.isEmpty()) {
				throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
			}
			return new RetextureRecipe(recipeId, group, width, height, nonnulllist, itemstack, keys);
		}

		@Override
		public RetextureRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			int width = buffer.readVarInt();
			int height = buffer.readVarInt();
			String group = buffer.readUtf(256);
			NonNullList<Ingredient> nonnulllist = NonNullList.withSize(width * height, Ingredient.EMPTY);
			for (int k = 0; k < nonnulllist.size(); ++k) {
				nonnulllist.set(k, Ingredient.fromNetwork(buffer));
			}
			ItemStack itemstack = buffer.readItem();

			String[] keys = new String[width * height];
			for (int i = 0; i < keys.length; i++) {
				keys[i] = buffer.readUtf(16);
			}
			return new RetextureRecipe(recipeId, group, width, height, nonnulllist, itemstack, keys);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, RetextureRecipe recipe) {
			buffer.writeVarInt(recipe.getRecipeWidth());
			buffer.writeVarInt(recipe.getRecipeHeight());
			buffer.writeUtf(recipe.getGroup(), 256);
			for (Ingredient ingredient : recipe.getIngredients()) {
				ingredient.toNetwork(buffer);
			}
			buffer.writeItem(recipe.getResultItem());

			for (String textureKey : recipe.textureKeys) {
				buffer.writeUtf(textureKey);
			}
		}
	}
}

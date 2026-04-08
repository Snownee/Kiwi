package snownee.kiwi.recipe.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import snownee.kiwi.data.DataModule;

public class NoContainersShapedRecipe extends CustomRecipe {
	private final String group;
	private final CraftingBookCategory category;
	private final ShapedRecipePattern pattern;
	private final ItemStack result;
	private final boolean showNotification;
	private final boolean noContainers;
	private final PlacementInfo placementInfo;

	public NoContainersShapedRecipe(
			String group,
			CraftingBookCategory category,
			ShapedRecipePattern pattern,
			ItemStack result,
			boolean showNotification,
			boolean noContainers) {
		this.group = group;
		this.category = category;
		this.pattern = pattern;
		this.result = result;
		this.showNotification = showNotification;
		this.noContainers = noContainers;
		this.placementInfo = PlacementInfo.createFromOptionals(pattern.ingredients());
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return pattern.matches(input);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return result.copy();
	}

	@Override
	public CraftingBookCategory category() {
		return category;
	}

	@Override
	public String group() {
		return group;
	}

	@Override
	public boolean isSpecial() {
		return false;
	}

	@Override
	public boolean showNotification() {
		return showNotification;
	}

	@Override
	public PlacementInfo placementInfo() {
		return placementInfo;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		if (noContainers) {
			return NonNullList.withSize(input.size(), ItemStack.EMPTY);
		}
		return super.getRemainingItems(input);
	}

	@Override
	public RecipeSerializer<NoContainersShapedRecipe> getSerializer() {
		return DataModule.SHAPED_NO_CONTAINERS.get();
	}

	public ShapedRecipePattern pattern() {
		return pattern;
	}

	public ItemStack result() {
		return result;
	}

	public boolean noContainers() {
		return noContainers;
	}

	public static class Serializer {
		public static final MapCodec<NoContainersShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Codec.STRING.optionalFieldOf("group", "").forGetter(NoContainersShapedRecipe::group),
				CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(NoContainersShapedRecipe::category),
				ShapedRecipePattern.MAP_CODEC.forGetter(NoContainersShapedRecipe::pattern),
				ItemStack.CODEC.fieldOf("result").forGetter(NoContainersShapedRecipe::result),
				Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(NoContainersShapedRecipe::showNotification),
				Codec.BOOL.optionalFieldOf("no_containers", false).forGetter(NoContainersShapedRecipe::noContainers)
		).apply(instance, NoContainersShapedRecipe::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, NoContainersShapedRecipe> STREAM_CODEC = StreamCodec.of(
				Serializer::toNetwork,
				Serializer::fromNetwork);

		public static NoContainersShapedRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			String group = buffer.readUtf();
			CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
			ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
			ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
			boolean showNotification = buffer.readBoolean();
			boolean noContainers = buffer.readBoolean();
			return new NoContainersShapedRecipe(group, category, pattern, result, showNotification, noContainers);
		}

		public static void toNetwork(RegistryFriendlyByteBuf buffer, NoContainersShapedRecipe recipe) {
			buffer.writeUtf(recipe.group);
			buffer.writeEnum(recipe.category);
			ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
			ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
			buffer.writeBoolean(recipe.showNotification);
			buffer.writeBoolean(recipe.noContainers);
		}
	}
}

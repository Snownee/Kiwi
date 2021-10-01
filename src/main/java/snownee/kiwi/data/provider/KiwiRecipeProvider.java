package snownee.kiwi.data.provider;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.data.recipes.UpgradeRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public abstract class KiwiRecipeProvider implements DataProvider {

	protected static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
	protected final DataGenerator generator;

	public KiwiRecipeProvider(DataGenerator generator) {
		this.generator = generator;
	}

	@Override
	public void run(HashCache hashCache) throws IOException {
		Path path = this.generator.getOutputFolder();
		Set<ResourceLocation> set = Sets.newHashSet();
		addRecipes(recipe -> {
			if (!set.add(recipe.getId())) {
				throw new IllegalStateException("Duplicate recipe " + recipe.getId());
			} else {
				save(hashCache, recipe.serializeRecipe(), path.resolve("data/" + recipe.getId().getNamespace() + "/recipes/" + recipe.getId().getPath() + ".json"));
				JsonObject jsonobject = recipe.serializeAdvancement();
				if (jsonobject != null) {
					save(hashCache, jsonobject, path.resolve("data/" + recipe.getId().getNamespace() + "/advancements/" + recipe.getAdvancementId().getPath() + ".json"));
				}
			}
		});
	}

	protected abstract void addRecipes(Consumer<FinishedRecipe> collector);

	protected void save(HashCache hashCache, JsonObject json, Path path) {
		try {
			String s = GSON.toJson((JsonElement) json);
			String s1 = SHA1.hashUnencodedChars(s).toString();
			if (!Objects.equals(hashCache.getHash(path), s1) || !Files.exists(path)) {
				Files.createDirectories(path.getParent());
				BufferedWriter bufferedwriter = Files.newBufferedWriter(path);

				try {
					bufferedwriter.write(s);
				} catch (Throwable throwable1) {
					if (bufferedwriter != null) {
						try {
							bufferedwriter.close();
						} catch (Throwable throwable) {
							throwable1.addSuppressed(throwable);
						}
					}
					throw throwable1;
				}

				if (bufferedwriter != null) {
					bufferedwriter.close();
				}
			}

			hashCache.putNew(path, s1);
		} catch (IOException ioexception) {
			LOGGER.error("Couldn't save recipe {}", path, ioexception);
		}
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	public static void oneToOneConversionRecipe(Consumer<FinishedRecipe> collector, ItemLike to, ItemLike from, @Nullable String group) {
		oneToOneConversionRecipe(collector, to, from, group, 1);
	}

	public static void oneToOneConversionRecipe(Consumer<FinishedRecipe> collector, ItemLike to, ItemLike from, @Nullable String group, int count) {
		ShapelessRecipeBuilder.shapeless(to, count).requires(from).group(group).unlockedBy(getHasName(from), has(from)).save(collector, getConversionRecipeName(to, from));
	}

	public static void oreSmelting(Consumer<FinishedRecipe> p_176592_, List<ItemLike> p_176593_, ItemLike p_176594_, float p_176595_, int p_176596_, String p_176597_) {
		oreCooking(p_176592_, RecipeSerializer.SMELTING_RECIPE, p_176593_, p_176594_, p_176595_, p_176596_, p_176597_, "_from_smelting");
	}

	public static void oreBlasting(Consumer<FinishedRecipe> p_176626_, List<ItemLike> p_176627_, ItemLike p_176628_, float p_176629_, int p_176630_, String p_176631_) {
		oreCooking(p_176626_, RecipeSerializer.BLASTING_RECIPE, p_176627_, p_176628_, p_176629_, p_176630_, p_176631_, "_from_blasting");
	}

	public static void oreCooking(Consumer<FinishedRecipe> p_176534_, SimpleCookingSerializer<?> p_176535_, List<ItemLike> p_176536_, ItemLike p_176537_, float p_176538_, int p_176539_, String p_176540_, String p_176541_) {
		for (ItemLike itemlike : p_176536_) {
			SimpleCookingRecipeBuilder.cooking(Ingredient.of(itemlike), p_176537_, p_176538_, p_176539_, p_176535_).group(p_176540_).unlockedBy(getHasName(itemlike), has(itemlike)).save(p_176534_, getItemName(p_176537_) + p_176541_ + "_" + getItemName(itemlike));
		}
	}

	public static void netheriteSmithing(Consumer<FinishedRecipe> p_125995_, Item p_125996_, Item p_125997_) {
		UpgradeRecipeBuilder.smithing(Ingredient.of(p_125996_), Ingredient.of(Items.NETHERITE_INGOT), p_125997_).unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT)).save(p_125995_, getItemName(p_125997_) + "_smithing");
	}

	public static void planksFromLog(Consumer<FinishedRecipe> p_125999_, ItemLike p_126000_, Tag<Item> p_126001_) {
		ShapelessRecipeBuilder.shapeless(p_126000_, 4).requires(p_126001_).group("planks").unlockedBy("has_log", has(p_126001_)).save(p_125999_);
	}

	public static void planksFromLogs(Consumer<FinishedRecipe> p_126018_, ItemLike p_126019_, Tag<Item> p_126020_) {
		ShapelessRecipeBuilder.shapeless(p_126019_, 4).requires(p_126020_).group("planks").unlockedBy("has_logs", has(p_126020_)).save(p_126018_);
	}

	public static void woodFromLogs(Consumer<FinishedRecipe> p_126003_, ItemLike p_126004_, ItemLike p_126005_) {
		ShapedRecipeBuilder.shaped(p_126004_, 3).define('#', p_126005_).pattern("##").pattern("##").group("bark").unlockedBy("has_log", has(p_126005_)).save(p_126003_);
	}

	public static void woodenBoat(Consumer<FinishedRecipe> p_126022_, ItemLike p_126023_, ItemLike p_126024_) {
		ShapedRecipeBuilder.shaped(p_126023_).define('#', p_126024_).pattern("# #").pattern("###").group("boat").unlockedBy("in_water", insideOf(Blocks.WATER)).save(p_126022_);
	}

	public static RecipeBuilder buttonBuilder(ItemLike p_176659_, Ingredient p_176660_) {
		return ShapelessRecipeBuilder.shapeless(p_176659_).requires(p_176660_);
	}

	public static RecipeBuilder doorBuilder(ItemLike p_176671_, Ingredient p_176672_) {
		return ShapedRecipeBuilder.shaped(p_176671_, 3).define('#', p_176672_).pattern("##").pattern("##").pattern("##");
	}

	public static RecipeBuilder fenceBuilder(ItemLike p_176679_, Ingredient p_176680_) {
		int i = p_176679_ == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
		Item item = p_176679_ == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
		return ShapedRecipeBuilder.shaped(p_176679_, i).define('W', p_176680_).define('#', item).pattern("W#W").pattern("W#W");
	}

	public static RecipeBuilder fenceGateBuilder(ItemLike p_176685_, Ingredient p_176686_) {
		return ShapedRecipeBuilder.shaped(p_176685_).define('#', Items.STICK).define('W', p_176686_).pattern("#W#").pattern("#W#");
	}

	public static void pressurePlate(Consumer<FinishedRecipe> p_176691_, ItemLike p_176692_, ItemLike p_176693_) {
		pressurePlateBuilder(p_176692_, Ingredient.of(p_176693_)).unlockedBy(getHasName(p_176693_), has(p_176693_)).save(p_176691_);
	}

	public static RecipeBuilder pressurePlateBuilder(ItemLike p_176695_, Ingredient p_176696_) {
		return ShapedRecipeBuilder.shaped(p_176695_).define('#', p_176696_).pattern("##");
	}

	public static void slab(Consumer<FinishedRecipe> p_176701_, ItemLike p_176702_, ItemLike p_176703_) {
		slabBuilder(p_176702_, Ingredient.of(p_176703_)).unlockedBy(getHasName(p_176703_), has(p_176703_)).save(p_176701_);
	}

	public static RecipeBuilder slabBuilder(ItemLike p_176705_, Ingredient p_176706_) {
		return ShapedRecipeBuilder.shaped(p_176705_, 6).define('#', p_176706_).pattern("###");
	}

	public static RecipeBuilder stairBuilder(ItemLike p_176711_, Ingredient p_176712_) {
		return ShapedRecipeBuilder.shaped(p_176711_, 4).define('#', p_176712_).pattern("#  ").pattern("## ").pattern("###");
	}

	public static RecipeBuilder trapdoorBuilder(ItemLike p_176721_, Ingredient p_176722_) {
		return ShapedRecipeBuilder.shaped(p_176721_, 2).define('#', p_176722_).pattern("###").pattern("###");
	}

	public static RecipeBuilder signBuilder(ItemLike p_176727_, Ingredient p_176728_) {
		return ShapedRecipeBuilder.shaped(p_176727_, 3).group("sign").define('#', p_176728_).define('X', Items.STICK).pattern("###").pattern("###").pattern(" X ");
	}

	public static void wall(Consumer<FinishedRecipe> p_176613_, ItemLike p_176614_, ItemLike p_176615_) {
		wallBuilder(p_176614_, Ingredient.of(p_176615_)).unlockedBy(getHasName(p_176615_), has(p_176615_)).save(p_176613_);
	}

	public static RecipeBuilder wallBuilder(ItemLike p_176515_, Ingredient p_176516_) {
		return ShapedRecipeBuilder.shaped(p_176515_, 6).define('#', p_176516_).pattern("###").pattern("###");
	}

	public static void polished(Consumer<FinishedRecipe> p_176641_, ItemLike p_176642_, ItemLike p_176643_) {
		polishedBuilder(p_176642_, Ingredient.of(p_176643_)).unlockedBy(getHasName(p_176643_), has(p_176643_)).save(p_176641_);
	}

	public static RecipeBuilder polishedBuilder(ItemLike p_176605_, Ingredient p_176606_) {
		return ShapedRecipeBuilder.shaped(p_176605_, 4).define('S', p_176606_).pattern("SS").pattern("SS");
	}

	public static void cut(Consumer<FinishedRecipe> p_176653_, ItemLike p_176654_, ItemLike p_176655_) {
		cutBuilder(p_176654_, Ingredient.of(p_176655_)).unlockedBy(getHasName(p_176655_), has(p_176655_)).save(p_176653_);
	}

	public static ShapedRecipeBuilder cutBuilder(ItemLike p_176635_, Ingredient p_176636_) {
		return ShapedRecipeBuilder.shaped(p_176635_, 4).define('#', p_176636_).pattern("##").pattern("##");
	}

	public static void chiseled(Consumer<FinishedRecipe> p_176665_, ItemLike p_176666_, ItemLike p_176667_) {
		chiseledBuilder(p_176666_, Ingredient.of(p_176667_)).unlockedBy(getHasName(p_176667_), has(p_176667_)).save(p_176665_);
	}

	public static ShapedRecipeBuilder chiseledBuilder(ItemLike p_176647_, Ingredient p_176648_) {
		return ShapedRecipeBuilder.shaped(p_176647_).define('#', p_176648_).pattern("#").pattern("#");
	}

	public static void stonecutterResultFromBase(Consumer<FinishedRecipe> p_176736_, ItemLike p_176737_, ItemLike p_176738_) {
		stonecutterResultFromBase(p_176736_, p_176737_, p_176738_, 1);
	}

	public static void stonecutterResultFromBase(Consumer<FinishedRecipe> p_176547_, ItemLike p_176548_, ItemLike p_176549_, int p_176550_) {
		SingleItemRecipeBuilder.stonecutting(Ingredient.of(p_176549_), p_176548_, p_176550_).unlockedBy(getHasName(p_176549_), has(p_176549_)).save(p_176547_, getConversionRecipeName(p_176548_, p_176549_) + "_stonecutting");
	}

	public static void smeltingResultFromBase(Consumer<FinishedRecipe> p_176740_, ItemLike p_176741_, ItemLike p_176742_) {
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(p_176742_), p_176741_, 0.1F, 200).unlockedBy(getHasName(p_176742_), has(p_176742_)).save(p_176740_);
	}

	public static void nineBlockStorageRecipes(Consumer<FinishedRecipe> p_176744_, ItemLike p_176745_, ItemLike p_176746_) {
		nineBlockStorageRecipes(p_176744_, p_176745_, p_176746_, getSimpleRecipeName(p_176746_), (String) null, getSimpleRecipeName(p_176745_), (String) null);
	}

	public static void nineBlockStorageRecipesWithCustomPacking(Consumer<FinishedRecipe> p_176563_, ItemLike p_176564_, ItemLike p_176565_, String p_176566_, String p_176567_) {
		nineBlockStorageRecipes(p_176563_, p_176564_, p_176565_, p_176566_, p_176567_, getSimpleRecipeName(p_176564_), (String) null);
	}

	public static void nineBlockStorageRecipesRecipesWithCustomUnpacking(Consumer<FinishedRecipe> p_176617_, ItemLike p_176618_, ItemLike p_176619_, String p_176620_, String p_176621_) {
		nineBlockStorageRecipes(p_176617_, p_176618_, p_176619_, getSimpleRecipeName(p_176619_), (String) null, p_176620_, p_176621_);
	}

	public static void nineBlockStorageRecipes(Consumer<FinishedRecipe> p_176569_, ItemLike p_176570_, ItemLike p_176571_, String p_176572_, @Nullable String p_176573_, String p_176574_, @Nullable String p_176575_) {
		ShapelessRecipeBuilder.shapeless(p_176570_, 9).requires(p_176571_).group(p_176575_).unlockedBy(getHasName(p_176571_), has(p_176571_)).save(p_176569_, new ResourceLocation(p_176574_));
		ShapedRecipeBuilder.shaped(p_176571_).define('#', p_176570_).pattern("###").pattern("###").pattern("###").group(p_176573_).unlockedBy(getHasName(p_176570_), has(p_176570_)).save(p_176569_, new ResourceLocation(p_176572_));
	}

	public static void simpleCookingRecipe(Consumer<FinishedRecipe> p_176584_, String p_176585_, SimpleCookingSerializer<?> p_176586_, int p_176587_, ItemLike p_176588_, ItemLike p_176589_, float p_176590_) {
		SimpleCookingRecipeBuilder.cooking(Ingredient.of(p_176588_), p_176589_, p_176590_, p_176587_, p_176586_).unlockedBy(getHasName(p_176588_), has(p_176588_)).save(p_176584_, getItemName(p_176589_) + "_from_" + p_176585_);
	}

	public static void waxRecipes(Consumer<FinishedRecipe> p_176611_) {
		HoneycombItem.WAXABLES.get().forEach((p_176578_, p_176579_) -> {
			ShapelessRecipeBuilder.shapeless(p_176579_).requires(p_176578_).requires(Items.HONEYCOMB).group(getItemName(p_176579_)).unlockedBy(getHasName(p_176578_), has(p_176578_)).save(p_176611_, getConversionRecipeName(p_176579_, Items.HONEYCOMB));
		});
	}

	protected static EnterBlockTrigger.TriggerInstance insideOf(Block p_125980_) {
		return new EnterBlockTrigger.TriggerInstance(EntityPredicate.Composite.ANY, p_125980_, StatePropertiesPredicate.ANY);
	}

	public static InventoryChangeTrigger.TriggerInstance has(MinMaxBounds.Ints p_176521_, ItemLike p_176522_) {
		return inventoryTrigger(ItemPredicate.Builder.item().of(p_176522_).withCount(p_176521_).build());
	}

	protected static InventoryChangeTrigger.TriggerInstance has(ItemLike p_125978_) {
		return inventoryTrigger(ItemPredicate.Builder.item().of(p_125978_).build());
	}

	protected static InventoryChangeTrigger.TriggerInstance has(Tag<Item> p_125976_) {
		return inventoryTrigger(ItemPredicate.Builder.item().of(p_125976_).build());
	}

	protected static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... p_126012_) {
		return new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, p_126012_);
	}

	public static String getHasName(ItemLike p_176603_) {
		return "has_" + getItemName(p_176603_);
	}

	@SuppressWarnings("deprecation")
	public static String getItemName(ItemLike p_176633_) {
		return Registry.ITEM.getKey(p_176633_.asItem()).getPath();
	}

	public static String getSimpleRecipeName(ItemLike p_176645_) {
		return getItemName(p_176645_);
	}

	public static String getConversionRecipeName(ItemLike p_176518_, ItemLike p_176519_) {
		return getItemName(p_176518_) + "_from_" + getItemName(p_176519_);
	}

	public static String getSmeltingRecipeName(ItemLike p_176657_) {
		return getItemName(p_176657_) + "_from_smelting";
	}

	public static String getBlastingRecipeName(ItemLike p_176669_) {
		return getItemName(p_176669_) + "_from_blasting";
	}
}

package snownee.kiwi.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.jspecify.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.Unpooled;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.network.connection.ConnectionType;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.block.loader.BlockCodecs;
import snownee.kiwi.data.DataModule;
import snownee.kiwi.recipe.AlternativesIngredient;
import snownee.kiwi.recipe.AlternativesIngredientBuilder;
import snownee.kiwi.recipe.CustomIngredient;
import snownee.kiwi.recipe.CustomIngredientImpl;
import snownee.kiwi.recipe.CustomIngredientSerializer;
import snownee.kiwi.recipe.KiwiRecipe;
import snownee.kiwi.recipe.RecipeUtil;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.kiwi.recipe.crafting.KiwiShapelessRecipe;
import snownee.kiwi.recipe.crafting.KiwiShapelessRecipeBuilder;
import snownee.kiwi.recipe.crafting.NoContainersShapedRecipe;
import snownee.kiwi.util.codec.KCodecs;

@EventBusSubscriber(modid = Kiwi.ID)
public final class RecipeGameTests {
	private static final Identifier ENVIRONMENT = Kiwi.id("alternatives");

	private RecipeGameTests() {
	}

	@SubscribeEvent
	public static void register(RegisterGameTestsEvent event) {
		Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(ENVIRONMENT);
		for (TestCase testCase : TestCase.values()) {
			TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
					environment,
					Identifier.withDefaultNamespace("empty"),
					200,
					0,
					true);
			event.registerTest(Kiwi.id("alternatives/" + testCase.id), new Instance(data, testCase));
		}
	}

	private static void run(TestCase testCase, GameTestHelper helper) {
		try {
			switch (testCase) {
				case CODEC_BUILDER -> codecBuilder(helper);
				case CODEC_SELECTION -> codecSelection(helper);
				case CODEC_INVALID -> codecInvalid(helper);
				case NETWORK -> network(helper);
				case EMPTY_SHAPELESS -> emptyShapeless(helper);
				case SHAPELESS_STABILITY -> shapelessStability(helper);
				case NO_REMAINDERS -> noRemainders(helper);
				case CONDITIONAL_CODEC -> conditionalCodec(helper);
				case LEGACY_COMPAT -> legacyCompat(helper);
				case KCODECS -> kCodecs();
			}
			String id = "kiwi:alternatives/" + testCase.id;
			Kiwi.LOGGER.info("KIWI_GAMETEST_PASS {}", id);
			helper.succeed();
		} catch (Throwable throwable) {
			Kiwi.LOGGER.error("Recipe GameTest {} failed", testCase.id, throwable);
			helper.fail(throwable.toString());
		}
	}

	private static RegistryOps<JsonElement> ops(GameTestHelper helper) {
		return helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
	}

	private static HolderLookup.RegistryLookup<net.minecraft.world.item.Item> items(GameTestHelper helper) {
		return helper.getLevel().registryAccess().lookupOrThrow(Registries.ITEM);
	}

	private static void codecBuilder(GameTestHelper helper) {
		RegistryOps<JsonElement> ops = ops(helper);
		AlternativesIngredientBuilder builder = AlternativesIngredientBuilder.of(items(helper))
				.add((ICustomIngredient) new DifferenceIngredient(Ingredient.of(Items.STONE), Ingredient.of(Items.COBBLESTONE)))
				.add(Items.DIRT)
				.allowEmpty();
		JsonElement input = AlternativesIngredientBuilder.Serializer.INSTANCE.codec().encodeStart(ops, builder).getOrThrow();
		AlternativesIngredientBuilder decodedBuilder = AlternativesIngredientBuilder.Serializer.INSTANCE.codec().parse(ops, input).getOrThrow();
		JsonElement inner = AlternativesIngredientBuilder.Serializer.INSTANCE.codec().encodeStart(ops, decodedBuilder).getOrThrow();
		JsonArray options = inner.getAsJsonObject().getAsJsonArray("options");
		check(options.size() == 3, "builder option count/order changed");
		JsonObject difference = options.get(0).getAsJsonObject();
		check(
				"neoforge:difference".equals(difference.get("neoforge:ingredient_type").getAsString()),
				"custom option discriminator changed");
		Ingredient decodedDifference = Ingredient.CODEC.parse(ops, difference).getOrThrow();
		check(decodedDifference.getCustomIngredient() instanceof DifferenceIngredient, "custom option failed to decode");
		Ingredient dirt = Ingredient.CODEC.parse(ops, options.get(1)).getOrThrow();
		check(dirt.test(Items.DIRT.getDefaultInstance()), "builder dirt option moved");
		check(!dirt.test(Items.STONE.getDefaultInstance()), "builder dirt option changed");
		check(options.get(2).isJsonArray() && options.get(2).getAsJsonArray().isEmpty(), "allowEmpty sentinel missing");
	}

	private static void codecSelection(GameTestHelper helper) {
		RegistryOps<JsonElement> ops = ops(helper);
		JsonObject input = parseObject("""
				{"neoforge:ingredient_type":"kiwi:alternatives","options":[
				 {"broken_candidate":"first_error"},
				 "#kiwi:missing_runtime_tag",
				 "minecraft:dirt",
				 "minecraft:stone"
				]}""");
		Ingredient ingredient = Ingredient.CODEC.parse(ops, input).getOrThrow();
		AlternativesIngredient alternatives = unwrap(ingredient);
		check(alternatives.test(Items.DIRT.getDefaultInstance()), "first runtime nonempty option was not selected");
		check(!alternatives.test(Items.STONE.getDefaultInstance()), "selection was not stable");
		Ingredient roundTrip = Ingredient.CODEC.parse(ops, Ingredient.CODEC.encodeStart(ops, ingredient).getOrThrow()).getOrThrow();
		check(roundTrip.test(Items.DIRT.getDefaultInstance()), "selection codec roundtrip failed");

		AlternativesIngredient selected = new AlternativesIngredient(List.of(
				new DifferenceIngredient(Ingredient.of(Items.STONE), Ingredient.of(Items.STONE)).toVanilla(),
				Ingredient.of(Items.DIRT)));
		check(selected.requiresTesting(), "custom candidate was not marked for testing before encode");
		JsonElement selectedEncoded = AlternativesIngredient.Serializer.INSTANCE.codec().encodeStart(ops, selected).getOrThrow();
		JsonArray selectedOptions = selectedEncoded.getAsJsonObject().getAsJsonArray("options");
		check(selectedOptions.size() == 1, "runtime encode retained unresolved options");
		Ingredient selectedOption = Ingredient.CODEC.parse(ops, selectedOptions.get(0)).getOrThrow();
		check(selectedOption.test(Items.DIRT.getDefaultInstance()), "runtime encode did not select dirt");
		check(!selectedOption.test(Items.STONE.getDefaultInstance()), "runtime encode selected stone");
		AlternativesIngredient selectedRoundTrip = AlternativesIngredient.Serializer.INSTANCE.codec().parse(ops, selectedEncoded).getOrThrow();
		check(selectedRoundTrip.test(Items.DIRT.getDefaultInstance()), "selected runtime roundtrip lost dirt");
		check(!selectedRoundTrip.test(Items.STONE.getDefaultInstance()), "selected runtime roundtrip gained stone");
		check(!selected.requiresTesting(), "selected vanilla wrapper retained custom testing requirement");

		ArrayList<Ingredient> mutableOptions = new ArrayList<>();
		mutableOptions.add(Ingredient.of(Items.STONE));
		AlternativesIngredient copied = new AlternativesIngredient(mutableOptions);
		mutableOptions.clear();
		check(copied.test(Items.STONE.getDefaultInstance()), "constructor did not defensively copy options");

		AtomicInteger queries = new AtomicInteger();
		CustomIngredient delayed = countingIngredient(queries, List.of(Items.STONE.getDefaultInstance()));
		AlternativesIngredient direct = new AlternativesIngredient(delayed.toVanilla());
		check(direct.requiresTesting(), "direct custom wrapper was not marked for testing");
		check(queries.get() == 0, "direct requiresTesting queried matching stacks");
		AlternativesIngredient lazy = new AlternativesIngredient(List.of(delayed.toVanilla(), Ingredient.of(Items.DIRT)));
		check(lazy.requiresTesting(), "custom candidate was not marked for testing");
		check(queries.get() == 0, "requiresTesting eagerly initialized a candidate");
		check(lazy.test(Items.STONE.getDefaultInstance()), "delayed custom candidate did not select");
		check(queries.get() == 1, "candidate initialization was not one-shot");
		check(lazy.test(Items.STONE.getDefaultInstance()) && queries.get() == 1, "selected candidate was queried again");

		AtomicInteger retries = new AtomicInteger();
		AlternativesIngredient retrying = new AlternativesIngredient(List.of(retryingIngredient(retries).toVanilla()));
		check(retrying.requiresTesting(), "retrying custom candidate was not marked for testing");
		check(!retrying.test(Items.STONE.getDefaultInstance()), "transient failure unexpectedly selected a candidate");
		check(retries.get() == 1 && retrying.requiresTesting(), "transient failure cleared unresolved state");
		check(retrying.test(Items.STONE.getDefaultInstance()), "candidate was not retried after transient failure");
		check(retries.get() == 2, "retry count changed after successful selection");
		check(retrying.test(Items.STONE.getDefaultInstance()) && retries.get() == 2, "successful retry queried candidate again");
	}

	private static void codecInvalid(GameTestHelper helper) {
		RegistryOps<JsonElement> ops = ops(helper);
		DataResult<Ingredient> empty = Ingredient.CODEC.parse(ops, parseObject(
				"{\"neoforge:ingredient_type\":\"kiwi:alternatives\",\"options\":[]}"));
		check(empty.isError(), "empty options decoded successfully");
		check(empty.error().orElseThrow().message().contains("No valid ingredient found"), "empty options error lost context");
		DataResult<Ingredient> malformed = Ingredient.CODEC.parse(ops, parseObject("""
				{"neoforge:ingredient_type":"kiwi:alternatives","options":[
				 {"item":"minecraft:air"}, {"tag":17}
				]}"""));
		check(malformed.isError(), "malformed alternatives decoded successfully");
		String error = malformed.error().orElseThrow().message();
		check(error.contains("No valid ingredient found") && error.contains(","), "candidate errors were not aggregated: " + error);
		DataResult<Ingredient> missing = Ingredient.CODEC.parse(ops, parseObject(
				"{\"neoforge:ingredient_type\":\"kiwi:alternatives\"}"));
		check(missing.isError() && missing.error().orElseThrow().message().contains("missing options"), "missing options error lost context");
		DataResult<Ingredient> nonList = Ingredient.CODEC.parse(ops, parseObject(
				"{\"neoforge:ingredient_type\":\"kiwi:alternatives\",\"options\":17}"));
		check(nonList.isError() && nonList.error().orElseThrow().message().contains("No valid ingredient found"), "non-list options error lost context");
	}

	private static void network(GameTestHelper helper) {
		var access = helper.getLevel().registryAccess();
		AlternativesIngredient vanilla = new AlternativesIngredient(Ingredient.of(Items.DIRT));
		AlternativesIngredient nested = new AlternativesIngredient(
				new DifferenceIngredient(Ingredient.of(Items.STONE), Ingredient.of(Items.COBBLESTONE)).toVanilla());
		AlternativesIngredient empty = new AlternativesIngredient((Ingredient) null);
		for (AlternativesIngredient original : List.of(vanilla, nested, empty)) {
			RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), access, ConnectionType.NEOFORGE);
			AlternativesIngredient.Serializer.STREAM_CODEC.encode(buffer, original);
			buffer.readerIndex(0);
			AlternativesIngredient decoded = AlternativesIngredient.Serializer.STREAM_CODEC.decode(buffer);
			check(decoded.getMatchingStacks().size() == original.getMatchingStacks().size(), "inner network codec changed items");
		}

		for (AlternativesIngredient original : List.of(vanilla, empty)) {
			RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), access, ConnectionType.NEOFORGE);
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, original.toVanilla());
			buffer.readerIndex(0);
			Ingredient decoded = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
			unwrap(decoded);
			check(decoded.isEmpty() == original.getMatchingStacks().isEmpty(), "outer network codec changed emptiness");
		}

		RegistryFriendlyByteBuf other = new RegistryFriendlyByteBuf(Unpooled.buffer(), access, ConnectionType.OTHER);
		Ingredient.CONTENTS_STREAM_CODEC.encode(other, vanilla.toVanilla());
		other.readerIndex(0);
		Ingredient downgraded = Ingredient.CONTENTS_STREAM_CODEC.decode(other);
		check(downgraded.getCustomIngredient() == null, "OTHER connection retained custom bridge");
		check(downgraded.test(Items.DIRT.getDefaultInstance()), "OTHER connection lost vanilla matching items");
	}

	private static void emptyShapeless(GameTestHelper helper) {
		Ingredient empty = Ingredient.CODEC.parse(ops(helper), parseObject(
				"{\"neoforge:ingredient_type\":\"kiwi:alternatives\",\"options\":[[]]}" )).getOrThrow();
		AlternativesIngredient alternatives = unwrap(empty);
		JsonElement encoded = Ingredient.CODEC.encodeStart(ops(helper), empty).getOrThrow();
		check(encoded.equals(parseObject(
				"{\"neoforge:ingredient_type\":\"kiwi:alternatives\",\"options\":[[]]}")), "empty sentinel JSON shape changed: " + encoded);
		Ingredient roundTrip = Ingredient.CODEC.parse(ops(helper), encoded).getOrThrow();
		check(unwrap(roundTrip).getMatchingStacks().isEmpty(), "empty sentinel roundtrip exposed items");
		check(!alternatives.test(Items.DIRT.getDefaultInstance()), "empty sentinel matched a stack");
		check(alternatives.getMatchingStacks().isEmpty(), "empty sentinel exposed items");
		check(alternatives.display() instanceof SlotDisplay.Empty, "empty sentinel display was not empty");
		ShapelessRecipe recipe = shapeless(List.of(empty));
		check(recipe.placementInfo().ingredients().isEmpty(), "placement retained empty Alternatives slot");
		check(!recipe.matches(CraftingInput.EMPTY, helper.getLevel()), "trimmed empty recipe unexpectedly matched");
		check(recipe.display().size() == 1, "display path did not complete");
	}

	private static void shapelessStability(GameTestHelper helper) {
		AtomicInteger checks = new AtomicInteger();
		AlternativesIngredient empty = new AlternativesIngredient((Ingredient) null) {
			@Override
			public List<ItemStack> getMatchingStacks() {
				checks.incrementAndGet();
				return List.of();
			}
		};
		ShapelessRecipe recipe = shapeless(List.of(empty.toVanilla()));
		check(recipe.placementInfo().ingredients().isEmpty(), "first trim failed");
		check(!recipe.matches(CraftingInput.EMPTY, helper.getLevel()), "trimmed empty recipe unexpectedly matched");
		recipe.display();
		recipe.placementInfo();
		recipe.matches(CraftingInput.EMPTY, helper.getLevel());
		check(checks.get() == 1, "empty Alternatives was tested more than once: " + checks.get());
	}

	private static void noRemainders(GameTestHelper helper) {
		RegistryOps<JsonElement> ops = ops(helper);
		JsonObject json = vanillaRecipeJson(true);
		Recipe<?> decoded = Recipe.CODEC.parse(ops, json).getOrThrow();
		check(decoded instanceof KiwiRecipe kiwiRecipe && kiwiRecipe.kiwi$noRemainders(), "recipe flag was not decoded");
		JsonObject encoded = Recipe.CODEC.encodeStart(ops, decoded).getOrThrow().getAsJsonObject();
		check(encoded.get("kiwi:no_remainders").getAsBoolean(), "recipe flag was not encoded");
		Recipe<?> roundTrip = Recipe.CODEC.parse(ops, encoded).getOrThrow();
		check(roundTrip instanceof KiwiRecipe kiwiRecipe && kiwiRecipe.kiwi$noRemainders(), "recipe flag roundtrip failed");
		Ingredient custom = ((ShapelessRecipe) roundTrip).placementInfo().ingredients().getFirst();
		unwrap(custom);

		CapturingOutput output = new CapturingOutput();
		ICondition condition = new ModLoadedCondition("kiwi");
		RecipeOutput wrappedOutput = RecipeUtil.withNoRemainders(output);
		wrappedOutput.includeRootAdvancement();
		wrappedOutput.advancement();
		wrappedOutput.accept(recipeKey("wrapped"), shapeless(List.of(Ingredient.of(Items.LAVA_BUCKET))), null, condition);
		check(output.recipe instanceof KiwiRecipe kiwiRecipe && kiwiRecipe.kiwi$noRemainders(), "RecipeOutput wrapper did not set flag");
		check(output.acceptCalls == 1 && output.conditions != null && output.conditions.length == 1 && output.conditions[0] == condition,
				"RecipeOutput wrapper did not forward conditions");
		check(output.advancementCalls == 1 && output.includeRootCalls == 1, "RecipeOutput wrapper did not forward advancement methods");
		Recipe<?> wrappedRoundTrip = Recipe.CODEC.parse(ops, Recipe.CODEC.encodeStart(ops, output.recipe).getOrThrow()).getOrThrow();
		CraftingInput input = CraftingInput.of(1, 1, List.of(Items.LAVA_BUCKET.getDefaultInstance()));
		NonNullList<ItemStack> withRemainder = shapeless(List.of(Ingredient.of(Items.LAVA_BUCKET))).getRemainingItems(input);
		check(withRemainder.getFirst().is(Items.BUCKET), "vanilla remainder baseline failed");
		NonNullList<ItemStack> none = ((CraftingRecipe) wrappedRoundTrip).getRemainingItems(input);
		check(none.size() == input.size() && none.stream().allMatch(ItemStack::isEmpty), "no-remainders mixin failed");
	}

	private static void conditionalCodec(GameTestHelper helper) {
		RegistryOps<JsonElement> ops = ops(helper);
		JsonObject json = vanillaRecipeJson(true);
		JsonArray conditions = new JsonArray();
		conditions.add(parseObject("{\"type\":\"neoforge:mod_loaded\",\"modid\":\"kiwi\"}"));
		json.add("neoforge:conditions", conditions);
		Optional<WithConditions<Recipe<?>>> decoded = Recipe.CONDITIONAL_CODEC.parse(ops, json).getOrThrow();
		check(decoded.isPresent(), "satisfied conditional recipe was absent");
		Recipe<?> recipe = decoded.orElseThrow().carrier();
		check(recipe instanceof KiwiRecipe kiwiRecipe && kiwiRecipe.kiwi$noRemainders(), "conditional codec lost extension flag");
		unwrap(((ShapelessRecipe) recipe).placementInfo().ingredients().getFirst());
		JsonObject encoded = Recipe.CONDITIONAL_CODEC.encodeStart(ops, decoded).getOrThrow().getAsJsonObject();
		check(encoded.has("neoforge:conditions") && encoded.has("kiwi:no_remainders"), "conditional encode lost fields");
		Optional<WithConditions<Recipe<?>>> roundTrip = Recipe.CONDITIONAL_CODEC.parse(ops, encoded).getOrThrow();
		check(roundTrip.isPresent() && roundTrip.orElseThrow().carrier() instanceof KiwiRecipe kiwiRecipe && kiwiRecipe.kiwi$noRemainders(),
				"conditional roundtrip failed");
	}

	private static void legacyCompat(GameTestHelper helper) {
		RegistryOps<JsonElement> ops = ops(helper);
		check(BuiltInRegistries.RECIPE_SERIALIZER.getValue(Kiwi.id("shapeless")) == DataModule.SHAPELESS.get(), "legacy shapeless serializer missing");
		check(BuiltInRegistries.RECIPE_SERIALIZER.getValue(Kiwi.id("shaped_no_containers")) == DataModule.SHAPED_NO_CONTAINERS.get(),
				"legacy shaped serializer missing");
		JsonObject shapelessJson = parseObject("""
				{"type":"kiwi:shapeless","ingredients":["minecraft:lava_bucket"],
				 "result":{"id":"minecraft:bucket"},"no_containers":true}""");
		Recipe<?> shapeless = Recipe.CODEC.parse(ops, shapelessJson).getOrThrow();
		check(shapeless instanceof KiwiShapelessRecipe, "legacy shapeless codec returned wrong type");
		CraftingInput input = CraftingInput.of(1, 1, List.of(Items.LAVA_BUCKET.getDefaultInstance()));
		check(((CraftingRecipe) shapeless).getRemainingItems(input).getFirst().isEmpty(), "legacy no_containers behavior changed");

		JsonObject shapedJson = parseObject("""
				{"type":"kiwi:shaped_no_containers","pattern":["A"],
				 "key":{"A":"minecraft:lava_bucket"},"result":{"id":"minecraft:bucket"},
				 "no_containers":true}""");
		Recipe<?> shaped = Recipe.CODEC.parse(ops, shapedJson).getOrThrow();
		check(shaped instanceof NoContainersShapedRecipe, "legacy shaped codec returned wrong type");
		check(((CraftingRecipe) shaped).getRemainingItems(input).getFirst().isEmpty(), "legacy shaped remainder changed");
		new KiwiShapelessRecipeBuilder(net.minecraft.data.recipes.RecipeCategory.MISC, Items.BUCKET, 1).noContainers();
		AlternativesIngredientBuilder.of(items(helper)).add((ICustomIngredient) new DifferenceIngredient(
				Ingredient.of(Items.STONE), Ingredient.of(Items.COBBLESTONE)));
	}

	private static void kCodecs() {
		check(KCodecs.tryCatch(() -> "success").getOrThrow().equals("success"), "tryCatch lost successful value");
		check(KCodecs.tryCatch(() -> {
			throw new Exception("checked");
		}).error().orElseThrow().message().equals("checked"), "tryCatch lost checked exception message");
		check(KCodecs.tryCatch(() -> {
			throw new IllegalStateException("runtime");
		}).error().orElseThrow().message().equals("runtime"), "tryCatch lost runtime exception message");
		AssertionError error = new AssertionError("error");
		try {
			KCodecs.tryCatch(() -> {
				throw error;
			});
			throw new IllegalStateException("tryCatch swallowed Error");
		} catch (AssertionError actual) {
			check(actual == error, "tryCatch changed Error");
		}
		try {
			KCodecs.<Object, Object>unsupportedGetter().apply(new Object());
			throw new IllegalStateException("unsupported getter returned");
		} catch (UnsupportedOperationException e) {
			check("Serialization is not supported for this field".equals(e.getMessage()), "unsupported getter message changed");
		}
		try {
			new SizedIngredient(Ingredient.of(Items.STONE), 0);
			throw new IllegalStateException("SizedIngredient accepted zero count");
		} catch (IllegalArgumentException e) {
			check("Size must be positive".equals(e.getMessage()), "SizedIngredient exception message changed");
		}
		try {
			BlockCodecs.STAIR.codec().encodeStart(
					JsonOps.INSTANCE,
					(StairBlock) Blocks.OAK_STAIRS).getOrThrow();
			throw new IllegalStateException("stair codec encoded unsupported base state");
		} catch (UnsupportedOperationException e) {
			check(e.getMessage() == null, "stair codec exception message changed");
		}
	}

	private static ShapelessRecipe shapeless(List<Ingredient> ingredients) {
		return new ShapelessRecipe(
				new Recipe.CommonInfo(true),
				new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
				new ItemStackTemplate(Items.BUCKET, 1),
				ingredients);
	}

	private static JsonObject vanillaRecipeJson(boolean noRemainders) {
		JsonObject json = parseObject("""
				{"type":"minecraft:crafting_shapeless","ingredients":[
				 {"neoforge:ingredient_type":"kiwi:alternatives","options":["minecraft:lava_bucket"]}
				],"result":{"id":"minecraft:bucket"}}""");
		json.addProperty("kiwi:no_remainders", noRemainders);
		return json;
	}

	private static ResourceKey<Recipe<?>> recipeKey(String path) {
		return ResourceKey.create(Registries.RECIPE, Kiwi.id(path));
	}

	private static JsonObject parseObject(String json) {
		return JsonParser.parseString(json).getAsJsonObject();
	}

	private static AlternativesIngredient unwrap(Ingredient ingredient) {
		check(ingredient.getCustomIngredient() instanceof CustomIngredientImpl<?>, "Ingredient did not use Kiwi NeoForge bridge");
		CustomIngredient custom = ((CustomIngredientImpl<?>) ingredient.getCustomIngredient()).ingredient();
		check(custom instanceof AlternativesIngredient, "Kiwi bridge did not contain AlternativesIngredient");
		return (AlternativesIngredient) custom;
	}

	private static CustomIngredient countingIngredient(AtomicInteger queries, List<ItemStack> stacks) {
		return new CustomIngredient() {
			@Override
			public boolean test(ItemStack stack) {
				return stacks.stream().anyMatch(candidate -> ItemStack.isSameItemSameComponents(candidate, stack));
			}

			@Override
			public List<ItemStack> getMatchingStacks() {
				queries.incrementAndGet();
				return stacks;
			}

			@Override
			public boolean requiresTesting() {
				return true;
			}

			@Override
			public CustomIngredientSerializer<?> getSerializer() {
				return AlternativesIngredient.Serializer.INSTANCE;
			}
		};
	}

	private static CustomIngredient retryingIngredient(AtomicInteger queries) {
		return new CustomIngredient() {
			@Override
			public boolean test(ItemStack stack) {
				return stack.is(Items.STONE);
			}

			@Override
			public List<ItemStack> getMatchingStacks() {
				if (queries.incrementAndGet() == 1) {
					throw new IllegalStateException("transient");
				}
				return List.of(Items.STONE.getDefaultInstance());
			}

			@Override
			public boolean requiresTesting() {
				return true;
			}

			@Override
			public CustomIngredientSerializer<?> getSerializer() {
				return AlternativesIngredient.Serializer.INSTANCE;
			}
		};
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}

	private enum TestCase {
		CODEC_BUILDER("codec_builder"),
		CODEC_SELECTION("codec_selection"),
		CODEC_INVALID("codec_invalid"),
		NETWORK("network"),
		EMPTY_SHAPELESS("empty_shapeless"),
		SHAPELESS_STABILITY("shapeless_stability"),
		NO_REMAINDERS("no_remainders"),
		CONDITIONAL_CODEC("conditional_codec"),
		LEGACY_COMPAT("legacy_compat"),
		KCODECS("kcodecs");

		private static final Codec<TestCase> CODEC = Codec.STRING.xmap(
				name -> valueOf(name.toUpperCase(Locale.ROOT)),
				testCase -> testCase.id);
		private final String id;

		TestCase(String id) {
			this.id = id;
		}
	}

	private static final class Instance extends GameTestInstance {
		private static final MapCodec<Instance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				TestData.CODEC.forGetter(Instance::info),
				TestCase.CODEC.fieldOf("case").forGetter(test -> test.testCase)
		).apply(instance, Instance::new));
		private final TestCase testCase;

		private Instance(TestData<Holder<TestEnvironmentDefinition<?>>> info, TestCase testCase) {
			super(info);
			this.testCase = testCase;
		}

		@Override
		public void run(GameTestHelper helper) {
			RecipeGameTests.run(testCase, helper);
		}

		@Override
		public MapCodec<? extends GameTestInstance> codec() {
			return CODEC;
		}

		@Override
		protected net.minecraft.network.chat.MutableComponent typeDescription() {
			return net.minecraft.network.chat.Component.literal(testCase.id);
		}
	}

	private static final class CapturingOutput implements RecipeOutput {
		private @Nullable Recipe<?> recipe;
		private @Nullable ICondition[] conditions;
		private int acceptCalls;
		private int advancementCalls;
		private int includeRootCalls;

		@Override
		public void accept(
				ResourceKey<Recipe<?>> id,
				Recipe<?> recipe,
				@Nullable AdvancementHolder advancement,
				ICondition... conditions) {
			this.recipe = recipe;
			this.conditions = conditions;
			acceptCalls++;
		}

		@Override
		public Advancement.Builder advancement() {
			advancementCalls++;
			return Advancement.Builder.advancement();
		}

		@Override
		public void includeRootAdvancement() {
			includeRootCalls++;
		}
	}
}

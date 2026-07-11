package snownee.kiwi.test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import snownee.kiwi.Kiwi;
import snownee.kiwi.customization.CustomizationMetadata;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.resource.OneTimeLoader;

@EventBusSubscriber(modid = Kiwi.ID)
public final class OneTimeLoaderGameTests {
	private static final Identifier ENVIRONMENT = Kiwi.id("one_time_loader");
	private static final Codec<String> VALUE_CODEC = Codec.STRING.fieldOf("value").codec();

	private OneTimeLoaderGameTests() {
	}

	@SubscribeEvent
	public static void register(RegisterGameTestsEvent event) {
		Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(ENVIRONMENT);
		for (TestCase testCase : TestCase.values()) {
			TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
					environment, Identifier.withDefaultNamespace("empty"), 200, 0, true);
			event.registerTest(Kiwi.id("one_time_loader/" + testCase.id), new Instance(data, testCase));
		}
	}

	private static void run(TestCase testCase, GameTestHelper helper) {
		try {
			switch (testCase) {
				case JSON_YAML -> jsonAndYaml(helper);
				case NATIVE -> nativeConditions(helper);
				case UNAVAILABLE -> unavailableContext(helper);
				case KIWI -> kiwiConditions(helper);
				case SHORT_CIRCUIT -> nativeShortCircuit(helper);
				case TAGS -> tagContext(helper);
				case METADATA -> metadataParity(helper);
			}
			Kiwi.LOGGER.info("KIWI_GAMETEST_PASS kiwi:one_time_loader/{}", testCase.id);
			helper.succeed();
		} catch (Throwable throwable) {
			Kiwi.LOGGER.error("OneTimeLoader GameTest {} failed", testCase.id, throwable);
			helper.fail(throwable.toString());
		}
	}

	private static OneTimeLoader.Context runtime(GameTestHelper helper) {
		return OneTimeLoader.Context.runtime(helper.getLevel().registryAccess(), FeatureFlags.DEFAULT_FLAGS, "GameTest");
	}

	private static void jsonAndYaml(GameTestHelper helper) {
		check("json".equals(parse("case.json", "{\"value\":\"json\"}", runtime(helper))), "JSON without conditions did not parse");
		check("yaml".equals(parse("case.yaml", "value: yaml", runtime(helper))), "YAML without conditions did not parse");
	}

	private static void nativeConditions(GameTestHelper helper) {
		OneTimeLoader.Context context = runtime(helper);
		String allow = "{\"neoforge:conditions\":[{\"type\":\"neoforge:mod_loaded\",\"modid\":\"kiwi\"}],\"value\":\"allow\"}";
		check("allow".equals(parse("allow.json", allow, context)), "satisfied native condition did not allow JSON");
		check(parseOptional("skip.yaml", "neoforge:conditions:\n  - type: neoforge:mod_loaded\n    modid: missing_kiwi_test_mod\nvalue: skip", context) == null,
				"unsatisfied native condition did not skip YAML");
		String malformed = "{\"neoforge:conditions\":\"not-an-array\",\"value\":\"bad\"}";
		check(OneTimeLoader.parseFile(file("malformed.json"), resource(malformed), VALUE_CODEC, context).error().isPresent(),
				"malformed native conditions did not return an error");
	}

	private static void unavailableContext(GameTestHelper helper) {
		OneTimeLoader.Context unavailable = OneTimeLoader.Context.unavailable(helper.getLevel().registryAccess(), "startup test");
		String nativeCondition = "{\"neoforge:conditions\":[{\"type\":\"neoforge:mod_loaded\",\"modid\":\"kiwi\"}],\"value\":\"value\"}";
		var result = OneTimeLoader.parseFile(file("unavailable.json"), resource(nativeCondition), VALUE_CODEC, unavailable);
		check(result != null && result.error().isPresent(), "unavailable context fell back to an empty native condition context");
		check(result != null && result.error().orElseThrow().message().contains("startup test"), "unavailable context error lost its stage");
	}

	private static void kiwiConditions(GameTestHelper helper) {
		OneTimeLoader.Context context = runtime(helper);
		check("namespaced".equals(parse("namespaced.json", "{\"kiwi:condition\":\"false\",\"condition\":\"true\",\"value\":\"namespaced\"}", context)),
				"kiwi:condition did not take precedence over legacy condition");
		check(parseOptional("kiwi-true.json", "{\"kiwi:condition\":\"true\",\"value\":\"skip\"}", context) == null,
				"true Kiwi condition did not skip");
		check("legacy".equals(parse("legacy.yaml", "condition: 'false'\nvalue: legacy", context)), "legacy false condition did not allow");
		check(parseOptional("legacy-true.yaml", "condition: 'true'\nvalue: skip", context) == null, "legacy true condition did not skip");
		check(OneTimeLoader.parseFile(file("non-string.json"), resource("{\"kiwi:condition\":true,\"value\":\"bad\"}"), VALUE_CODEC, context).error().isPresent(),
				"non-string Kiwi condition did not return an error");
	}

	private static void nativeShortCircuit(GameTestHelper helper) {
		String input = "{\"neoforge:conditions\":[{\"type\":\"neoforge:mod_loaded\",\"modid\":\"missing_kiwi_test_mod\"}],\"kiwi:condition\":\"not valid EvalEx\",\"value\":\"skip\"}";
		var result = OneTimeLoader.parseFile(file("short-circuit.json"), resource(input), VALUE_CODEC, runtime(helper));
		check(result == null, "native skip did not short-circuit malformed Kiwi condition");
	}

	private static void tagContext(GameTestHelper helper) {
		var context = Platform.conditionContext(helper.getLevel().registryAccess(), FeatureFlags.DEFAULT_FLAGS);
		TagKey<Item> present = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("planks"));
		TagKey<Item> absent = TagKey.create(Registries.ITEM, Kiwi.id("missing_game_test_tag"));
		check(context.isTagLoaded(present) && !context.getTag(present).isEmpty(), "runtime condition context did not expose a loaded item tag");
		check(!context.isTagLoaded(absent) && context.getTag(absent).isEmpty(), "runtime condition context reported a missing item tag");
	}

	private static void metadataParity(GameTestHelper helper) {
		String allow = "{\"neoforge:conditions\":[{\"type\":\"neoforge:mod_loaded\",\"modid\":\"kiwi\"}],\"registry_order\":{\"item\":[\"minecraft:stone\"]}}";
		var allowed = OneTimeLoader.parseFile(file("metadata.json"), resource(allow), CustomizationMetadata.CODEC, runtime(helper));
		check(allowed != null && allowed.error().isEmpty() && allowed.result().orElseThrow().registryOrder().containsKey("item"),
				"metadata codec did not share the native allow path");
		String skip = "{\"neoforge:conditions\":[{\"type\":\"neoforge:mod_loaded\",\"modid\":\"missing_kiwi_test_mod\"}],\"registry_order\":{}}";
		check(OneTimeLoader.parseFile(file("metadata-skip.json"), resource(skip), CustomizationMetadata.CODEC, runtime(helper)) == null,
				"metadata codec did not share the native skip path");
	}

	private static String parse(String path, String contents, OneTimeLoader.Context context) {
		var result = parseOptional(path, contents, context);
		check(result != null && result.error().isEmpty(), "failed to parse " + path + ": " + (result == null ? "skipped" : result.error()));
		return result.result().orElseThrow();
	}

	@SuppressWarnings("DataFlowIssue")
	private static com.mojang.serialization.@Nullable DataResult<String> parseOptional(
			String path, String contents, OneTimeLoader.Context context) {
		return OneTimeLoader.parseFile(file(path), resource(contents), VALUE_CODEC, context);
	}

	private static Identifier file(String path) {
		return Kiwi.id("game_test/" + path);
	}

	@SuppressWarnings("DataFlowIssue")
	private static Resource resource(String contents) {
		return new Resource(null, () -> new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)));
	}

	private static void check(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}

	private enum TestCase {
		JSON_YAML("json_yaml"),
		NATIVE("native"),
		UNAVAILABLE("unavailable"),
		KIWI("kiwi"),
		SHORT_CIRCUIT("short_circuit"),
		TAGS("tags"),
		METADATA("metadata");

		private static final Codec<TestCase> CODEC = Codec.STRING.xmap(name -> valueOf(name.toUpperCase(Locale.ROOT)), testCase -> testCase.id);
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
			OneTimeLoaderGameTests.run(testCase, helper);
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
}

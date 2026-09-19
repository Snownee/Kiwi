package snownee.kiwi.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

class LangRuleTest {
	private static LangRule parse(String json) {
		JsonElement element = JsonParser.parseString(json);
		DataResult<LangRule> result = LangRule.CODEC.parse(JsonOps.INSTANCE, element);
		return result.result().orElseThrow(() -> new AssertionError(result.error().orElseThrow().message()));
	}

	@Test
	void singleStringPrefix() {
		LangRule rule = parse("{\"prefixes\": \"a.\"}");
		assertEquals(List.of("a."), rule.prefixes());
		assertEquals(Map.of(), rule.define());
	}

	@Test
	void arrayPrefixWithDefine() {
		LangRule rule = parse("{\"prefixes\": [\"a.\", \"b.\"], \"define\": {\"jei\": \"has:jei\"}}");
		assertEquals(List.of("a.", "b."), rule.prefixes());
		assertEquals(Map.of("jei", "has:jei"), rule.define());
	}
}

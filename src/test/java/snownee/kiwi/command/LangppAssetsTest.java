package snownee.kiwi.command;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import snownee.kiwi.lang.LangRule;
import snownee.kiwi.lang.Template;

class LangppAssetsTest {
	private static JsonObject readJson(String path) throws Exception {
		try (InputStream in = LangppAssetsTest.class.getResourceAsStream(path)) {
			assertNotNull(in, "Missing test asset: " + path);
			return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
		}
	}

	@Test
	void languageTemplatesParse() throws Exception {
		for (String language : List.of("en_us", "zh_cn")) {
			JsonObject json = readJson("/assets/test/lang/" + language + ".json");
			for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
				String value = entry.getValue().getAsString();
				if (value.contains("<#")) {
					assertDoesNotThrow(
							() -> Template.parse(value),
							language + ":" + entry.getKey() + " = " + value);
				}
			}
		}
	}

	@Test
	void commandKeysExist() throws Exception {
		JsonObject json = readJson("/assets/test/lang/en_us.json");
		for (String key : LangppCommand.KEYS) {
			assertTrue(json.has(key), "Missing translation for command key: " + key);
		}
	}

	@Test
	void rulesParse() throws Exception {
		JsonObject json = readJson("/assets/test/kiwi/langpp/example.json");
		DataResult<LangRule> result = LangRule.CODEC.parse(JsonOps.INSTANCE, json);
		assertTrue(result.result().isPresent(), "Failed to parse test rules");
		assertEquals(List.of("langpp.test."), result.result().get().prefixes());
	}
}

package snownee.kiwi.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import snownee.kiwi.config.ConfigHandler.Value;
import snownee.kiwi.config.KiwiConfig.ConfigType;

class KiwiConfigManagerTest {
	@Test
	void resolveByFileName() {
		ConfigHandler handler = new ConfigHandler("test", "kiwi-common-test", ConfigType.COMMON, null, false);
		handler.define("eval.printExpression", Boolean.FALSE, null, "test");

		Value<?> raw = KiwiConfigManager.getValue("kiwi-common-test.eval.printExpression");
		assertNotNull(raw);
		assertEquals(Boolean.FALSE, raw.get());

		Value<?> normalized = KiwiConfigManager.getValue("kiwi.common.test.eval.printExpression");
		assertNotNull(normalized);
		assertEquals(raw, normalized);

		assertNull(KiwiConfigManager.getValue("kiwi-common-test.eval.missing"));
		assertNull(KiwiConfigManager.getValue("unknown.eval.printExpression"));
	}
}

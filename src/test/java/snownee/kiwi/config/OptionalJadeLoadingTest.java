package snownee.kiwi.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OptionalJadeLoadingTest {

	@Test
	void loadsWithoutJade() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		Assertions.assertNotNull(Class.forName("snownee.kiwi.config.ConfigScreens", false, classLoader));
		Assertions.assertNotNull(Class.forName("snownee.kiwi.config.ModMenuIntegration", false, classLoader));
	}
}

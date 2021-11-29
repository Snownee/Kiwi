package snownee.kiwi.loader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.io.Closeables;
import com.google.gson.Gson;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

public class AnnotatedTypeLoader implements Supplier<KiwiConfiguration> {

	public final String modId;

	public AnnotatedTypeLoader(String modId) {
		this.modId = modId;
	}

	@Override
	public KiwiConfiguration get() {
		Map<String, Object> properties = ModList.get().getModContainerById(modId).map(ModContainer::getModInfo).map(IModInfo::getModProperties).orElse(Collections.EMPTY_MAP);
		boolean useJson = (Boolean) properties.getOrDefault("kiwiJsonMap", Boolean.valueOf(Platform.isProduction()));
		if (!useJson) {
			return new DevEnvAnnotatedTypeLoader(modId).get();
		}
		String name = "/%s.kiwi.json".formatted(modId);
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		if (is == null) {
			return null;
		}
		InputStreamReader isr = new InputStreamReader(is);
		try {
			return new Gson().fromJson(isr, KiwiConfiguration.class);
		} finally {
			Closeables.closeQuietly(isr);
		}
	}

}

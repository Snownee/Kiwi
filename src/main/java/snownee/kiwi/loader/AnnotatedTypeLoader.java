package snownee.kiwi.loader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Supplier;

import com.google.common.io.Closeables;
import com.google.gson.Gson;

public class AnnotatedTypeLoader implements Supplier<KiwiConfiguration> {

	public final String modId;

	public AnnotatedTypeLoader(String modId) {
		this.modId = modId;
	}

	@Override
	public KiwiConfiguration get() {
		String name = "/%s.kiwi.json".formatted(modId);
		InputStream is = getClass().getResourceAsStream(name);
		//		InputStream is = FabricLoader.getInstance().getModContainer(modId).get().getRootPath() .getResourceAsStream(name);
		//		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);

		//		System.out.println(is);
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

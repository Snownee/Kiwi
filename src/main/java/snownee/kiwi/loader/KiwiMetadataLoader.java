package snownee.kiwi.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import snownee.kiwi.build.KiwiMetadata;
import snownee.kiwi.build.KiwiMetadataParser;

public record KiwiMetadataLoader(String modId) implements Function<KiwiMetadataParser, KiwiMetadata> {

	@Override
	public KiwiMetadata apply(KiwiMetadataParser parser) {
		String name = "/%s.kiwi.yaml".formatted(modId);
		try (InputStream is = getClass().getResourceAsStream(name)) {
			if (is == null) {
				return null;
			}
			return parser.load(is);
		} catch (IOException e) {
			return null;
		}
	}
}

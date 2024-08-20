package snownee.kiwi.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import net.neoforged.fml.ModList;
import snownee.kiwi.build.KiwiMetadata;
import snownee.kiwi.build.KiwiMetadataParser;

public record KiwiMetadataLoader(String modId) implements Function<KiwiMetadataParser, KiwiMetadata> {

	@Override
	public KiwiMetadata apply(KiwiMetadataParser parser) {
		String name = "/%s.kiwi.yaml".formatted(modId);
		Path p = ModList.get().getModFileById(modId).getFile().findResource(name);
		try (InputStream is = Files.newInputStream(p)) {
			if (is == null) {
				return null;
			}
			return parser.load(is);
		} catch (IOException e) {
			return null;
		}
	}
}

package snownee.kiwi.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.neoforged.fml.ModList;
import snownee.kiwi.build.KiwiMetadata;
import snownee.kiwi.build.KiwiMetadataParser;

public record KiwiMetadataLoader(String modId) implements Function<KiwiMetadataParser, @Nullable KiwiMetadata> {

	@Override
	public @Nullable KiwiMetadata apply(KiwiMetadataParser parser) {
		String name = "/%s.kiwi.yaml".formatted(modId);
		try (InputStream is = ModList.get().getModFileById(modId).getFile().getContents().openFile(name)) {
			if (is == null) {
				return null;
			}
			return parser.load(is);
		} catch (IOException e) {
			return null;
		}
	}
}

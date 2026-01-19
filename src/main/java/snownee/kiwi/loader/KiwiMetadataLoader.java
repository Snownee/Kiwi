package snownee.kiwi.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.fabricmc.loader.api.FabricLoader;
import snownee.kiwi.build.KiwiMetadata;
import snownee.kiwi.build.KiwiMetadataParser;

public record KiwiMetadataLoader(String modId) implements Function<KiwiMetadataParser, @Nullable KiwiMetadata> {

	@Override
	public @Nullable KiwiMetadata apply(KiwiMetadataParser parser) {
		String name = "%s.kiwi.yaml".formatted(modId);
		return FabricLoader.getInstance().getModContainer(modId).flatMap(mod -> mod.findPath(name)).map(path -> {
			try (InputStream is = Files.newInputStream(path)) {
				return parser.load(is);
			} catch (IOException e) {
				return null;
			}
		}).orElse(null);
	}
}

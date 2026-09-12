package snownee.kiwi.mixin.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Options;
import snownee.kiwi.Kiwi;

@Mixin(Options.class)
public class OptionsMixin {
	@Unique
	private static final Path GLOBAL_OPTIONS_PATH = Path.of(System.getProperty("user.home"), ".snownee", "options.txt");

	@Shadow
	@Final
	private File optionsFile;
	@Unique
	private boolean kiwi$hasGlobalOptions;

	@WrapOperation(method = "load", at = @At(value = "INVOKE", target = "Ljava/io/File;exists()Z"))
	private boolean kiwi$optionsFileExists(File file, Operation<Boolean> original) {
		if (file != optionsFile) {
			return original.call(file);
		}
		kiwi$hasGlobalOptions = Files.exists(GLOBAL_OPTIONS_PATH);
		return original.call(file) || kiwi$hasGlobalOptions;
	}

	@WrapOperation(
			method = "load",
			at = @At(
					value = "INVOKE",
					target = "Lcom/google/common/io/Files;newReader(Ljava/io/File;Ljava/nio/charset/Charset;)Ljava/io/BufferedReader;"))
	private BufferedReader kiwi$readOptions(File file, Charset charset, Operation<BufferedReader> original) throws IOException {
		if (!kiwi$hasGlobalOptions) {
			return original.call(file, charset);
		}
		Kiwi.LOGGER.info("Loading global options from {}", GLOBAL_OPTIONS_PATH);
		String global;
		try {
			global = Files.readString(GLOBAL_OPTIONS_PATH, charset);
		} catch (IOException e) {
			Kiwi.LOGGER.warn("Failed to read global options from {}", GLOBAL_OPTIONS_PATH, e);
			return original.call(file, charset);
		}
		if (!file.exists()) {
			return new BufferedReader(new StringReader(global));
		}
		String local = Files.readString(file.toPath(), charset);
		return new BufferedReader(new StringReader(mergeOptions(local, global)));
	}

	@Unique
	private static String mergeOptions(String local, String global) {
		boolean localHasVersion = local.lines().anyMatch(line -> line.startsWith("version:"));
		StringBuilder sb = new StringBuilder(local);
		if (!local.isEmpty() && !local.endsWith("\n")) {
			sb.append('\n');
		}
		for (String line : global.split("\n", -1)) {
			if (line.isEmpty()) {
				continue;
			}
			// Let the local file's version govern datafixing; global options still win.
			if (localHasVersion && line.startsWith("version:")) {
				continue;
			}
			sb.append(line).append('\n');
		}
		return sb.toString();
	}

	@Inject(method = "save", at = @At("TAIL"))
	private void kiwi$saveGlobalOptions(CallbackInfo ci) {
		if (!kiwi$hasGlobalOptions) {
			return;
		}
		try {
			Map<String, String> localOptions = parseOptions(optionsFile.toPath());
			List<String> lines = Files.readAllLines(GLOBAL_OPTIONS_PATH, StandardCharsets.UTF_8);
			int changed = 0;
			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				int idx = line.indexOf(':');
				if (idx <= 0) {
					continue;
				}
				String key = line.substring(0, idx);
				String value = localOptions.get(key);
				if (value != null && !value.equals(line.substring(idx + 1))) {
					lines.set(i, key + ":" + value);
					changed++;
				}
			}
			if (changed > 0) {
				Files.write(GLOBAL_OPTIONS_PATH, lines, StandardCharsets.UTF_8);
				Kiwi.LOGGER.info("Updated {} global options in {}", changed, GLOBAL_OPTIONS_PATH);
			}
		} catch (IOException e) {
			Kiwi.LOGGER.warn("Failed to update global options in {}", GLOBAL_OPTIONS_PATH, e);
		}
	}

	@Unique
	private static Map<String, String> parseOptions(Path path) throws IOException {
		Map<String, String> map = Maps.newHashMap();
		for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
			int idx = line.indexOf(':');
			if (idx > 0) {
				map.put(line.substring(0, idx), line.substring(idx + 1));
			}
		}
		return map;
	}
}
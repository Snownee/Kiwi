package snownee.kiwi.datagen;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.util.Util;

public class KiwiLanguageProvider implements DataProvider {
	protected final FabricDataOutput dataOutput;
	protected final String languageCode;

	protected KiwiLanguageProvider(FabricDataOutput dataOutput) {
		this(dataOutput, "en_us");
	}

	protected KiwiLanguageProvider(FabricDataOutput dataOutput, String languageCode) {
		this.dataOutput = dataOutput;
		this.languageCode = languageCode;
	}

	/**
	 * Implement this method to register languages.
	 *
	 * <p>Call {@link FabricLanguageProvider.TranslationBuilder#add(String, String)} to add a translation.
	 */
	public void generateTranslations(FabricLanguageProvider.TranslationBuilder translationBuilder) {

	}

	public void putExistingTranslations(FabricLanguageProvider.TranslationBuilder translationBuilder) {
		try {
			Path existingFilePath = createPath(languageCode + ".existing");
			translationBuilder.add(existingFilePath);
		} catch (Exception e) {
			throw new RuntimeException("Failed to add existing language file!", e);
		}
	}

	public Path createPath(String path) {
		return this.dataOutput.getForgeModContainer().getOwningFile().getFile().findResource("assets/%s/lang/%s.json".formatted(dataOutput.getModId(), path));
	}

	@Override
	public CompletableFuture<?> run(CachedOutput writer) {
		TreeMap<String, String> translationEntries = new TreeMap<>();

		generateConfigEntries(translationEntries);
		generateTranslations((String key, String value) -> {
			Objects.requireNonNull(key);
			Objects.requireNonNull(value);

			if (translationEntries.containsKey(key)) {
				throw new RuntimeException("Existing translation key found - " + key + " - Duplicate will be ignored.");
			}

			translationEntries.put(key, value);
		});

		putExistingTranslations((String key, String value) -> {
			Objects.requireNonNull(key);
			Objects.requireNonNull(value);
			translationEntries.put(key, value);
		});

		JsonObject langEntryJson = new JsonObject();

		for (Map.Entry<String, String> entry : translationEntries.entrySet()) {
			langEntryJson.addProperty(entry.getKey(), entry.getValue());
		}

		return DataProvider.saveStable(writer, langEntryJson, getLangFilePath(this.languageCode));
	}

	private void generateConfigEntries(Map<String, String> translationEntries) {
		Joiner joiner = Joiner.on('.');
		for (ConfigHandler handler : KiwiConfigManager.allConfigs) {
			if (!Objects.equals(handler.getModId(), dataOutput.getModId())) {
				continue;
			}
			if (handler.getFileName().equals("test") || handler.getFileName().equals("kiwi-modules")) {
				continue;
			}
			String key = handler.getTranslationKey();
			if (!Objects.equals(key, handler.getFileName())) {
				translationEntries.put("kiwi.config." + key, Util.friendlyText(key));
			}
			Set<String> subCats = Sets.newHashSet();
			for (ConfigHandler.Value<?> value : handler.getValueMap().values()) {
				ConfigUI.Hide hide = value.getAnnotation(ConfigUI.Hide.class);
				if (hide != null) {
					continue;
				}
				List<String> path = Lists.newArrayList(value.path.split("\\."));
				String title = Util.friendlyText(path.remove(path.size() - 1));
				String subCatKey = joiner.join(path);
				if (!path.isEmpty() && !subCats.contains(subCatKey)) {
					subCats.add(subCatKey);
					translationEntries.put(handler.getModId() + ".config." + subCatKey, Util.friendlyText(path.get(path.size() - 1)));
				}
				translationEntries.put(value.translation, title);
				translationEntries.put(value.translation + ".desc", "");
			}
		}
	}

	private Path getLangFilePath(String code) {
		return dataOutput
				.createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang")
				.json(new ResourceLocation(dataOutput.getModId(), code));
	}

	@Override
	public String getName() {
		return "Language (%s)".formatted(languageCode);
	}

}

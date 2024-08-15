package snownee.kiwi.datagen;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.util.Util;

public class KiwiLanguageProvider extends FabricLanguageProvider {
	protected final FabricDataOutput dataOutput;
	protected final String languageCode;

	public KiwiLanguageProvider(FabricDataOutput dataOutput) {
		this(dataOutput, "en_us");
	}

	public KiwiLanguageProvider(FabricDataOutput dataOutput, String languageCode) {
		super(dataOutput, languageCode);
		this.dataOutput = dataOutput;
		this.languageCode = languageCode;
	}

	/**
	 * Implement this method to register languages.
	 *
	 * <p>Call {@link FabricLanguageProvider.TranslationBuilder#add(String, String)} to add a translation.
	 */
	@Override
	public void generateTranslations(FabricLanguageProvider.TranslationBuilder translationBuilder) {
	}

	public Optional<Path> createPath(String path, String extension) {
		return this.dataOutput.getModContainer()
				.findPath("assets/%s/lang/%s.%s".formatted(dataOutput.getModId(), path, extension));
	}

	public void putExistingTranslations(FabricLanguageProvider.TranslationBuilder translationBuilder) {
		putExistingTranslations(translationBuilder, languageCode + ".existing");
	}

	public void putExistingTranslations(FabricLanguageProvider.TranslationBuilder translationBuilder, String path) {
		try {
			Path existingFilePath = createPath(path, "json").orElseThrow();
			translationBuilder.add(existingFilePath);
		} catch (Exception e) {
			throw new RuntimeException("Failed to add existing language file!", e);
		}
	}

	public void putExistingYamlTranslations(FabricLanguageProvider.TranslationBuilder translationBuilder) {
		putExistingYamlTranslations(translationBuilder, languageCode + ".existing");
	}

	public void putExistingYamlTranslations(FabricLanguageProvider.TranslationBuilder translationBuilder, String path) {
		try {
			Path existingFilePath = createPath(path, "yaml").orElseThrow();
			try (Reader reader = Files.newBufferedReader(existingFilePath)) {
				Map<String, ?> map = Util.loadYaml(reader, Map.class);
				for (Map.Entry<String, ?> entry : map.entrySet()) {
					translationBuilder.add(entry.getKey(), entry.getValue().toString());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to add existing language file!", e);
		}
	}

	@Override
	public CompletableFuture<?> run(CachedOutput writer) {
		TreeMap<String, String> translationEntries = new TreeMap<>();
		preGenerate(translationEntries);
		if ("en_us".equals(languageCode)) {
			generateGameObjectsEntries(translationEntries);
			generateConfigEntries(translationEntries);
			generateTranslations((String key, String value) -> {
				Objects.requireNonNull(key);
				Objects.requireNonNull(value);

				if (translationEntries.containsKey(key)) {
					throw new RuntimeException("Existing translation key found - " + key + " - Duplicate will be ignored.");
				}

				translationEntries.put(key, value);
			});

			FabricLanguageProvider.TranslationBuilder translationBuilder = (String key, String value) -> {
				Objects.requireNonNull(key);
				Objects.requireNonNull(value);
				translationEntries.put(key, value);
			};
			if (createPath("en_us.existing", "yaml").map(Files::exists).orElse(false)) {
				putExistingYamlTranslations(translationBuilder);
			} else if (createPath("en_us.existing", "json").map(Files::exists).orElse(false)) {
				putExistingTranslations(translationBuilder);
			}
		}
		postGenerate(translationEntries);

		JsonObject langEntryJson = new JsonObject();

		for (Map.Entry<String, String> entry : translationEntries.entrySet()) {
			langEntryJson.addProperty(entry.getKey(), entry.getValue());
		}

		return DataProvider.saveStable(writer, langEntryJson, getLangFilePath(this.languageCode));
	}

	protected void postGenerate(TreeMap<String, String> translationEntries) {}

	protected void preGenerate(TreeMap<String, String> translationEntries) {}

	protected void generateConfigEntries(Map<String, String> translationEntries) {
		Joiner joiner = Joiner.on('.');
		for (ConfigHandler handler : KiwiConfigManager.allConfigs) {
			if (!Objects.equals(handler.getModId(), dataOutput.getModId())) {
				continue;
			}
			if (handler.getFileName().equals("test") || handler.getFileName().equals("kiwi-modules")) {
				continue; // skip test entries
			}
			if (handler.getClazz().getDeclaredAnnotation(KiwiModule.Skip.class) != null) {
				continue;
			}
			String key = handler.getTranslationKey();
			if (Objects.equals(key, handler.getFileName())) {
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
		return dataOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang").json(new ResourceLocation(
				dataOutput.getModId(),
				code));
	}

	protected void generateGameObjectsEntries(Map<String, String> translationEntries) {
		generateGameObjectEntries(translationEntries, Registries.BLOCK, Block::getDescriptionId);
		generateGameObjectEntries(translationEntries, Registries.ITEM, Item::getDescriptionId);
		generateGameObjectEntries(translationEntries, Registries.ENTITY_TYPE, EntityType::getDescriptionId);
		generateGameObjectEntries(translationEntries, Registries.CREATIVE_MODE_TAB, tab -> {
			Component component = tab.getDisplayName();
			if (component.getContents() instanceof TranslatableContents contents) {
				return contents.getKey();
			} else {
				return null;
			}
		});
		generateGameObjectEntries(translationEntries, Registries.CUSTOM_STAT, stat -> net.minecraft.Util.makeDescriptionId("stat", stat));
		generateGameObjectEntries(translationEntries, Registries.MOB_EFFECT, MobEffect::getDescriptionId);
	}

	protected <T> void generateGameObjectEntries(
			Map<String, String> translationEntries,
			ResourceKey<Registry<T>> registryKey,
			Function<T, String> keyMapper) {
		GameObjectLookup.allHolders(registryKey, dataOutput.getModId()).forEach(holder -> {
			String key = keyMapper.apply(holder.value());
			if (key != null) {
				translationEntries.put(key, Util.friendlyText(holder.key().location().getPath()));
			}
		});
	}

}

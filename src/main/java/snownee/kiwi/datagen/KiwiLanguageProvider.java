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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.GameObjectLookup;
import snownee.kiwi.util.KUtil;

public class KiwiLanguageProvider extends LanguageProvider {
	@FunctionalInterface
	public interface TranslationBuilder {
		void add(String key, String value);
	}

	protected final String modId;
	protected final String languageCode;
	protected final CompletableFuture<HolderLookup.Provider> registryLookup;

	public KiwiLanguageProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
		this(output, Kiwi.ID, "en_us", registryLookup);
	}

	public KiwiLanguageProvider(PackOutput output, String modId, CompletableFuture<HolderLookup.Provider> registryLookup) {
		this(output, modId, "en_us", registryLookup);
	}

	public KiwiLanguageProvider(PackOutput output, String modId, String languageCode, CompletableFuture<HolderLookup.Provider> registryLookup) {
		super(output, modId, languageCode);
		this.modId = modId;
		this.languageCode = languageCode;
		this.registryLookup = registryLookup;
	}

	@Override
	protected void addTranslations() {
		TreeMap<String, String> translationEntries = new TreeMap<>();
		HolderLookup.Provider lookup = registryLookup.join();
		preGenerate(translationEntries);
		generateModNameAndDescription(translationEntries);
		generateConfigEntries(translationEntries);
		generateTranslations(lookup, (key, value) -> {
			Objects.requireNonNull(key);
			Objects.requireNonNull(value);
			if (translationEntries.containsKey(key)) {
				throw new RuntimeException("Existing translation key found - " + key + " - Duplicate will be ignored.");
			}
			translationEntries.put(key, value);
		});
		if (createPath(languageCode + ".existing", "yaml").map(Files::exists).orElse(false)) {
			putExistingYamlTranslations(translationEntries);
		} else if (createPath(languageCode + ".existing", "json").map(Files::exists).orElse(false)) {
			putExistingTranslations(translationEntries);
		}
		postGenerate(translationEntries);
		translationEntries.forEach(this::add);
	}

	public void generateTranslations(HolderLookup.Provider lookup, TranslationBuilder translationBuilder) {
	}

	public Optional<Path> createPath(String path, String extension) {
		return Platform.findResource(modId, "assets/%s/lang/%s.%s".formatted(modId, path, extension));
	}

	public void putExistingTranslations(Map<String, String> translationEntries) {
		putExistingTranslations(translationEntries, languageCode + ".existing");
	}

	public void putExistingTranslations(Map<String, String> translationEntries, String path) {
		try {
			Path existingFilePath = createPath(path, "json").orElseThrow();
			try (Reader reader = Files.newBufferedReader(existingFilePath)) {
				JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
					translationEntries.put(entry.getKey(), entry.getValue().getAsString());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to add existing language file!", e);
		}
	}

	public void putExistingYamlTranslations(Map<String, String> translationEntries) {
		putExistingYamlTranslations(translationEntries, languageCode + ".existing");
	}

	public void putExistingYamlTranslations(Map<String, String> translationEntries, String path) {
		try {
			Path existingFilePath = createPath(path, "yaml").orElseThrow();
			try (Reader reader = Files.newBufferedReader(existingFilePath)) {
				Map<String, ?> map = KUtil.loadYaml(reader, Map.class);
				for (Map.Entry<String, ?> entry : map.entrySet()) {
					translationEntries.put(entry.getKey(), entry.getValue().toString());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to add existing language file!", e);
		}
	}

	protected void postGenerate(TreeMap<String, String> translationEntries) {}

	protected void preGenerate(TreeMap<String, String> translationEntries) {}

	protected void generateConfigEntries(Map<String, String> translationEntries) {
		for (ConfigHandler handler : KiwiConfigManager.allConfigs) {
			if (!Objects.equals(handler.getModId(), modId)) {
				continue;
			}
			String fileName = handler.getFileName();
			if (fileName.equals("test") || fileName.equals("kiwi-modules")) {
				continue;
			}
			if (handler.getClazz() != null && handler.getClazz().getDeclaredAnnotation(KiwiModule.Skip.class) != null) {
				continue;
			}
			String key = handler.getTranslationKey();
			if (Objects.equals(key, fileName)) {
				translationEntries.put("kiwi.config." + key, KUtil.friendlyText(key));
			}
			Set<String> subCats = Sets.newHashSet();
			for (ConfigHandler.Value<?> value : handler.getValueMap().values()) {
				if (value.path.startsWith("modules.test")) {
					continue;
				}
				ConfigUI.Hide hide = value.getAnnotation(ConfigUI.Hide.class);
				if (hide != null) {
					continue;
				}
				List<String> path = Lists.newArrayList(value.path.split("\\."));
				String title = KUtil.friendlyText(path.removeLast());
				String subCatKey = String.join(".", path);
				if (!path.isEmpty() && !subCats.contains(subCatKey)) {
					subCats.add(subCatKey);
					translationEntries.put(handler.getModId() + ".config." + subCatKey, KUtil.friendlyText(path.getLast()));
				}
				translationEntries.put(value.translation, title);
				translationEntries.put(value.translation + ".desc", "");
			}
		}
	}

	protected void generateGameObjectsEntries(HolderLookup.Provider lookup, Map<String, String> translationEntries) {
		generateGameObjectEntries(translationEntries, lookup, Registries.BLOCK, Block::getDescriptionId);
		generateGameObjectEntries(translationEntries, lookup, Registries.ITEM, Item::getDescriptionId);
		generateGameObjectEntries(translationEntries, lookup, Registries.ENTITY_TYPE, EntityType::getDescriptionId);
		generateGameObjectEntries(
				translationEntries, lookup, Registries.CREATIVE_MODE_TAB, tab -> {
					Component component = tab.getDisplayName();
					if (component.getContents() instanceof TranslatableContents contents) {
						return contents.getKey();
					}
					return null;
				});
		generateGameObjectEntries(translationEntries, lookup, Registries.CUSTOM_STAT, stat -> net.minecraft.util.Util.makeDescriptionId("stat", stat));
		generateGameObjectEntries(translationEntries, lookup, Registries.MOB_EFFECT, MobEffect::getDescriptionId);
	}

	protected void generateModNameAndDescription(Map<String, String> translationEntries) {
		translationEntries.put("modmenu.nameTranslation.%s".formatted(modId), Platform.getModName(modId));
		String description = Platform.getModDescription(modId);
		translationEntries.put("modmenu.descriptionTranslation.%s".formatted(modId), description);
		translationEntries.put("fml.menu.mods.info.description.%s".formatted(modId), description);
	}

	protected <T> void generateGameObjectEntries(
			Map<String, String> translationEntries,
			HolderLookup.Provider lookup,
			ResourceKey<Registry<T>> registryKey,
			Function<T, String> keyMapper) {
		GameObjectLookup.allHolders(lookup, registryKey, modId).forEach(holder -> {
			String key = keyMapper.apply(holder.value());
			if (key != null) {
				translationEntries.put(key, KUtil.friendlyText(holder.key().identifier().getPath()));
			}
		});
	}
}
package snownee.kiwi.datagen;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.loader.api.ModContainer;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.ConfigUI;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.util.GameObjectLookup;
import snownee.kiwi.util.Util;

public class KiwiLanguageProvider implements DataProvider {
	protected final FabricDataOutput dataOutput;
	protected final String languageCode;

	public KiwiLanguageProvider(FabricDataOutput dataOutput) {
		this(dataOutput, "en_us");
	}

	public KiwiLanguageProvider(FabricDataOutput dataOutput, String languageCode) {
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
		return dataOutput.getModContainer().findPath("assets/%s/lang/%s.json".formatted(dataOutput.getModId(), path)).orElseThrow();
	}

	@Override
	public CompletableFuture<?> run(CachedOutput writer) {
		TreeMap<String, String> translationEntries = new TreeMap<>();

		generateModNameAndDescription(translationEntries);
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

	protected void generateConfigEntries(Map<String, String> translationEntries) {
		for (ConfigHandler handler : KiwiConfigManager.allConfigs) {
			if (!Objects.equals(handler.getModId(), dataOutput.getModId())) {
				continue;
			}
			if (handler.getFileName().equals("test") || handler.getFileName().equals("kiwi-modules")) {
				continue; // skip test entries
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
				String subCatKey = String.join(".", path);
				if (!path.isEmpty() && !subCats.contains(subCatKey)) {
					subCats.add(subCatKey);
					translationEntries.put(handler.getModId() + ".config." + subCatKey, Util.friendlyText(path.get(path.size() - 1)));
				}
				translationEntries.put(value.translation, title);
				translationEntries.put(value.translation + ".desc", "");
			}
		}
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
		generateGameObjectEntries(translationEntries, Registries.ENCHANTMENT, Enchantment::getDescriptionId);
		generateGameObjectEntries(translationEntries, Registries.CUSTOM_STAT, stat -> net.minecraft.Util.makeDescriptionId("stat", stat));
		generateGameObjectEntries(translationEntries, Registries.MOB_EFFECT, MobEffect::getDescriptionId);
	}

	protected void generateModNameAndDescription(Map<String, String> translationEntries) {
		ModContainer container = dataOutput.getModContainer();
		String modId = dataOutput.getModId();
		translationEntries.put("modmenu.nameTranslation.%s".formatted(modId), container.getMetadata().getName());
		translationEntries.put("modmenu.descriptionTranslation.%s".formatted(modId), container.getMetadata().getDescription());
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

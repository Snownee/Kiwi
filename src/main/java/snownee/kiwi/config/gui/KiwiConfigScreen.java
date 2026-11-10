package snownee.kiwi.config.gui;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import snownee.jade.gui.BaseOptionsScreen;
import snownee.jade.gui.config.OptionsList;
import snownee.kiwi.config.ConfigHandler;
import snownee.kiwi.config.ConfigHandler.Value;
import snownee.kiwi.config.ConfigUI.Hide;
import snownee.kiwi.config.ConfigUI.TextDescription;
import snownee.kiwi.util.KUtil;

/**
 * A configuration screen for Kiwi's YAML configs built on top of Jade's options screen. Jade is an
 * optional dependency, so this class is only loaded by {@link snownee.kiwi.config.JadeConfigIntegration}
 * when Jade is installed.
 */
public class KiwiConfigScreen extends BaseOptionsScreen {

	private final String namespace;
	private final List<ConfigHandler> configs;

	public KiwiConfigScreen(@Nullable Screen parent, String namespace, List<ConfigHandler> configs) {
		super(parent, Component.translatable("gui.kiwi.config"));
		this.namespace = namespace;
		this.configs = configs;
		saver = () -> configs.forEach(ConfigHandler::save);
		canceller = () -> {};
	}

	@Override
	public OptionsList createOptions(OptionsList optionsList) {
		for (ConfigHandler config : configs) {
			Component configTitle = configTitle(config);
			optionsList.add(new OptionsList.Title(namespace, configTitle));
			Map<String, OptionsList.Title> subTitles = Maps.newHashMap();
			for (Value<?> value : config.getValueMap().values()) {
				if (value.getAnnotation(Hide.class) != null) {
					continue;
				}
				List<String> path = Lists.newArrayList(value.path.split("\\."));
				String titleKey = path.removeLast();
				String subPath = String.join(".", path);
				if (!subPath.isEmpty()) {
					subTitles.computeIfAbsent(subPath, $ -> {
						OptionsList.Title title = new OptionsList.Title(namespace, subTitle(configTitle, subPath));
						optionsList.add(title);
						return title;
					});
				}

				TextDescription description = value.getAnnotation(TextDescription.class);
				if (description != null && !description.after()) {
					addDescription(optionsList, description.value());
				}

				ConfigEntry entry = ConfigEntry.create(namespace, value, valueTitle(value, titleKey), this::updateSaveState);
				if (entry != null) {
					value.createComment().ifPresent(comments -> {
						for (Component comment : comments) {
							entry.appendDescription(comment);
						}
					});
					if (!Double.isNaN(value.min) || !Double.isNaN(value.max)) {
						entry.appendDescription(ConfigEntry.rangeTooltip(value));
					}
					optionsList.add(entry);
				}

				if (description != null && description.after()) {
					addDescription(optionsList, description.value());
				}
			}
		}
		return optionsList;
	}

	private void addDescription(OptionsList optionsList, String key) {
		String text = Language.getInstance().getOrDefault(key);
		if (text.indexOf('\n') < 0 && text.indexOf('\r') < 0) {
			Component component = Language.getInstance().has(key) ? Component.translatable(key) : Component.literal(text);
			optionsList.add(new OptionsList.Entry(namespace, component));
			return;
		}
		for (String line : text.split("\\R", -1)) {
			optionsList.add(new OptionsList.Entry(namespace, Component.literal(line)));
		}
	}

	private void updateSaveState() {
		if (options != null) {
			options.updateSaveState();
		}
	}

	private Component configTitle(ConfigHandler config) {
		String key = "kiwi.config." + config.getTranslationKey();
		return Language.getInstance().has(key) ?
				Component.translatable(key) :
				Component.literal(KUtil.friendlyText(config.getTranslationKey()));
	}

	private Component subTitle(Component parent, String path) {
		String key = namespace + ".config." + path;
		Component child;
		if (Language.getInstance().has(key)) {
			child = Component.translatable(key);
		} else {
			String[] split = path.split("\\.");
			child = Component.literal(KUtil.friendlyText(split[split.length - 1]));
		}
		return Component.translatable("gui.kiwi.config.subtitle", parent, child);
	}

	private Component valueTitle(Value<?> value, String fallback) {
		return Language.getInstance().has(value.translation) ?
				Component.translatable(value.translation) :
				Component.literal(KUtil.friendlyText(fallback));
	}
}

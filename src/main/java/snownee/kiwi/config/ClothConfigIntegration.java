package snownee.kiwi.config;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.impl.builders.DoubleFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.FloatFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.TextFieldBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import snownee.kiwi.config.ConfigHandler.Value;

public class ClothConfigIntegration {

	public static Screen create(Screen parent, String namespace) {
		ConfigBuilder builder = ConfigBuilder.create();
		builder.setParentScreen(parent);
		List<ConfigHandler> configs = KiwiConfigManager.allConfigs.stream().filter($ -> $.getModId().equals(namespace)).toList();
		for (ConfigHandler config : configs) {
			Component title;
			if (config.getFileName().equals(config.getModId() + "-" + config.getType().extension())) {
				title = new TranslatableComponent("kiwi.config." + config.getType().extension());
			} else if (config.getFileName().equals(config.getModId() + "-modules")) {
				title = new TranslatableComponent("kiwi.config.modules");
			} else {
				title = new TextComponent(StringUtils.capitalize(config.getFileName()));
			}
			ConfigCategory category = builder.getOrCreateCategory(title);
			for (Value<?> value : config.valueMap.values()) {
				ConfigEntryBuilder entryBuilder = builder.entryBuilder();
				AbstractConfigListEntry<?> entry = null;
				if (value.value.getClass() == Boolean.class) {
					BooleanToggleBuilder toggle = entryBuilder.startBooleanToggle(value.component, (Boolean) value.value);
					toggle.setTooltip(createComment(value));
					toggle.setSaveConsumer($ -> value.accept($, config.onChanged));
					entry = toggle.build();
				} else if (value.value.getClass() == Integer.class) {
					IntFieldBuilder field = entryBuilder.startIntField(value.component, (Integer) value.value);
					field.setTooltip(createComment(value));
					field.setSaveConsumer($ -> value.accept($, config.onChanged));
					entry = field.build();
				} else if (value.value.getClass() == Double.class) {
					DoubleFieldBuilder field = entryBuilder.startDoubleField(value.component, (Double) value.value);
					field.setTooltip(createComment(value));
					field.setSaveConsumer($ -> value.accept($, config.onChanged));
					entry = field.build();
				} else if (value.value.getClass() == Float.class) {
					FloatFieldBuilder field = entryBuilder.startFloatField(value.component, (Float) value.value);
					field.setTooltip(createComment(value));
					field.setSaveConsumer($ -> value.accept($, config.onChanged));
					entry = field.build();
				} else if (value.value.getClass() == String.class) {
					//TODO: better Enum
					TextFieldBuilder field = entryBuilder.startTextField(value.component, (String) value.value);
					field.setTooltip(createComment(value));
					field.setSaveConsumer($ -> value.accept($, config.onChanged));
					entry = field.build();
				}
				if (entry != null) {
					entry.setRequiresRestart(value.requiresRestart);
					category.addEntry(entry);
				}
			}
		}
		builder.setSavingRunnable(() -> {
			configs.forEach(ConfigHandler::save);
		});
		return builder.build();
	}

	private static Optional<Component[]> createComment(Value<?> value) {
		if (value.comment == null || value.comment.length == 0) {
			return Optional.empty();
		}
		Component[] ret = new Component[value.comment.length];
		for (int i = 0; i < value.comment.length; i++) {
			String comment = value.comment[i];
			ret[i] = new TextComponent(comment);
		}
		return Optional.of(ret);
	}

}

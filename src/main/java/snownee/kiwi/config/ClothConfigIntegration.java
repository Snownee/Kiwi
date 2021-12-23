package snownee.kiwi.config;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import me.shedaniel.clothconfig2.impl.builders.ColorFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.DoubleFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.FloatFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.IntSliderBuilder;
import me.shedaniel.clothconfig2.impl.builders.LongFieldBuilder;
import me.shedaniel.clothconfig2.impl.builders.LongSliderBuilder;
import me.shedaniel.clothconfig2.impl.builders.TextFieldBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import snownee.kiwi.KiwiClientConfig;
import snownee.kiwi.config.ConfigHandler.Value;
import snownee.kiwi.config.ConfigUI.Color;
import snownee.kiwi.config.ConfigUI.Hide;
import snownee.kiwi.config.ConfigUI.Slider;

public class ClothConfigIntegration {

	private static final Component requiresRestart = new TranslatableComponent("kiwi.config.requiresRestart").withStyle(ChatFormatting.RED);

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
				Hide hide = value.getAnnotation(Hide.class);
				if (hide != null) {
					continue;
				}
				ConfigEntryBuilder entryBuilder = builder.entryBuilder();
				AbstractConfigListEntry<?> entry = null;
				Class<?> type = value.getType();
				if (type == boolean.class) {
					BooleanToggleBuilder toggle = entryBuilder.startBooleanToggle(value.component, (Boolean) value.value);
					toggle.setTooltip(createComment(value));
					toggle.setSaveConsumer($ -> value.accept($, config.onChanged));
					entry = toggle.build();
				} else if (type == int.class) {
					Color color = value.getAnnotation(Color.class);
					if (color != null) {
						ColorFieldBuilder field = entryBuilder.startAlphaColorField(value.component, (Integer) value.value);
						field.setAlphaMode(color.alpha());
						field.setTooltip(createComment(value));
						field.setSaveConsumer($ -> value.accept($, config.onChanged));
						entry = field.build();
					} else if (value.getAnnotation(Slider.class) != null) {
						IntSliderBuilder field = entryBuilder.startIntSlider(value.component, (Integer) value.value, (int) value.min, (int) value.max);
						field.setTooltip(createComment(value));
						field.setSaveConsumer($ -> value.accept($, config.onChanged));
						entry = field.build();
					} else {
						IntFieldBuilder field = entryBuilder.startIntField(value.component, (Integer) value.value);
						field.setTooltip(createComment(value));
						if (!Double.isNaN(value.min)) {
							field.setMin((int) value.min);
						}
						if (!Double.isNaN(value.max)) {
							field.setMax((int) value.max);
						}
						field.setSaveConsumer($ -> value.accept($, config.onChanged));
						entry = field.build();
					}
				} else if (type == double.class) {
					DoubleFieldBuilder field = entryBuilder.startDoubleField(value.component, (Double) value.value);
					field.setTooltip(createComment(value));
					if (!Double.isNaN(value.min)) {
						field.setMin(value.min);
					}
					if (!Double.isNaN(value.max)) {
						field.setMax(value.max);
					}
					field.setSaveConsumer($ -> value.accept($, config.onChanged));
					entry = field.build();
				} else if (type == float.class) {
					FloatFieldBuilder field = entryBuilder.startFloatField(value.component, (Float) value.value);
					field.setTooltip(createComment(value));
					if (!Double.isNaN(value.min)) {
						field.setMin((float) value.min);
					}
					if (!Double.isNaN(value.max)) {
						field.setMax((float) value.max);
					}
					field.setSaveConsumer($ -> value.accept($, config.onChanged));
					entry = field.build();
				} else if (type == long.class) {
					if (value.getAnnotation(Slider.class) != null) {
						LongSliderBuilder field = entryBuilder.startLongSlider(value.component, (Long) value.value, (long) value.min, (long) value.max);
						field.setTooltip(createComment(value));
						field.setSaveConsumer($ -> value.accept($, config.onChanged));
						entry = field.build();
					} else {
						LongFieldBuilder field = entryBuilder.startLongField(value.component, (Long) value.value);
						field.setTooltip(createComment(value));
						if (!Double.isNaN(value.min)) {
							field.setMin((long) value.min);
						}
						if (!Double.isNaN(value.max)) {
							field.setMax((long) value.max);
						}
						field.setSaveConsumer($ -> value.accept($, config.onChanged));
						entry = field.build();
					}
				} else if (type == String.class) {
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
		List<Component> tooltip = Lists.newArrayList();
		if (value.comment != null) {
			for (String comment : value.comment) {
				tooltip.add(new TextComponent(comment));
			}
		}
		if (value.requiresRestart) {
			tooltip.add(requiresRestart);
		}

		Font fontRenderer = Minecraft.getInstance().font;
		int width = KiwiClientConfig.tooltipWrapWidth;
		/* off */
		tooltip = tooltip.stream()
			.map(s -> fontRenderer.getSplitter().splitLines(s, width, Style.EMPTY))
			.flatMap(Collection::stream)
			.map(FormattedText::getString)
			.map(TextComponent::new)
			.collect(Collectors.toList());
		/* on */
		return tooltip.isEmpty() ? Optional.empty() : Optional.of(tooltip.toArray(new Component[0]));
	}

}

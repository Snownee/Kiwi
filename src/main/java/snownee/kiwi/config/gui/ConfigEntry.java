package snownee.kiwi.config.gui;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import snownee.jade.gui.NotUglyEditBox;
import snownee.jade.gui.config.OptionsList;
import snownee.jade.gui.config.value.OptionValue;
import snownee.kiwi.config.ConfigHandler.Value;
import snownee.kiwi.config.ConfigUI.Color;
import snownee.kiwi.config.ConfigUI.Slider;
import snownee.kiwi.config.ConfigUI.Typed;
import snownee.kiwi.util.LocalizableItem;

/**
 * A single config option rendered with Jade's option list layout. The class deliberately extends
 * Jade's {@link OptionValue} so Jade's list can validate and save entries like it does for its own
 * options.
 */
public class ConfigEntry extends OptionValue<Object> {

	private final Value<?> configValue;
	private final Runnable saver;
	private final BooleanSupplier validator;

	private ConfigEntry(
			String namespace,
			Component title,
			@Nullable AbstractWidget widget,
			Value<?> configValue,
			Runnable saver,
			BooleanSupplier validator) {
		super(namespace, "option", () -> null, _ -> {});
		this.configValue = configValue;
		this.saver = saver;
		this.validator = validator;
		// OptionValue builds Jade-specific titles from the namespace and option name. Replace them
		// with the title and messages supplied by Kiwi's config system.
		description = List.of();
		getMessages().clear();
		setTitle(title);
		addMessage(title.getString());
		if (widget != null) {
			addWidget(widget, 0);
		}
	}

	public static @Nullable ConfigEntry create(
			String namespace,
			Value<?> value,
			Component title,
			Runnable updateSaveState) {
		Class<?> type = value.getType();
		if (type == boolean.class) {
			CycleButton<Boolean> button = CycleButton.booleanBuilder(
							OptionsList.OPTION_ON,
							OptionsList.OPTION_OFF,
							(Boolean) value.value)
					.displayOnlyValue()
					.create(0, 0, 100, 20, title, (_, _) -> {});
			return new ConfigEntry(namespace, title, button, value, () -> value.accept(button.getValue()), () -> true);
		}
		if (type == int.class) {
			return createInt(namespace, value, title, updateSaveState);
		}
		if (type == long.class) {
			return createLong(namespace, value, title, updateSaveState);
		}
		if (type == double.class) {
			NotUglyEditBox box = createTextBox(title, String.valueOf(value.value), ConfigEntry::isDouble, updateSaveState);
			return new ConfigEntry(namespace, title, box, value, () -> {
				if (isDouble(box.getValue())) {
					value.accept(Double.parseDouble(box.getValue().replace(',', '.')));
				}
			}, () -> isDouble(box.getValue()));
		}
		if (type == float.class) {
			NotUglyEditBox box = createTextBox(title, String.valueOf(value.value), ConfigEntry::isFloat, updateSaveState);
			return new ConfigEntry(namespace, title, box, value, () -> {
				if (isFloat(box.getValue())) {
					value.accept(Float.parseFloat(box.getValue().replace(',', '.')));
				}
			}, () -> isFloat(box.getValue()));
		}
		if (type == String.class) {
			NotUglyEditBox box = createTextBox(title, (String) value.value, _ -> true, updateSaveState);
			return new ConfigEntry(namespace, title, box, value, () -> value.accept(box.getValue()), () -> true);
		}
		if (Enum.class.isAssignableFrom(type)) {
			return createEnum(namespace, value, title, type);
		}
		if (value.field != null && List.class.isAssignableFrom(type)) {
			Typed typed = value.field.getAnnotation(Typed.class);
			if (typed != null && typed.value() == String.class) {
				return createStringList(namespace, value, title, updateSaveState);
			}
		}
		return null;
	}

	private static @Nullable ConfigEntry createInt(String namespace, Value<?> value, Component title, Runnable updateSaveState) {
		Color color = value.getAnnotation(Color.class);
		if (color != null) {
			String initial = colorToString((Integer) value.value, color.alpha());
			Predicate<String> validator = color.alpha() ? ConfigEntry::isColorWithAlpha : ConfigEntry::isColor;
			NotUglyEditBox box = createTextBox(title, initial, validator, updateSaveState);
			return new ConfigEntry(namespace, title, box, value, () -> {
				if (validator.test(box.getValue())) {
					value.accept(parseColor(box.getValue()));
				}
			}, () -> validator.test(box.getValue()));
		}
		SliderResult slider = createIntSlider(title, (Integer) value.value, value);
		if (slider != null) {
			return new ConfigEntry(namespace, title, slider.widget(), value, () -> value.accept(sliderIntValue(slider, value)), () -> true);
		}
		NotUglyEditBox box = createTextBox(title, String.valueOf(value.value), ConfigEntry::isInteger, updateSaveState);
		return new ConfigEntry(namespace, title, box, value, () -> {
			if (isInteger(box.getValue())) {
				value.accept(Integer.parseInt(box.getValue()));
			}
		}, () -> isInteger(box.getValue()));
	}

	private static @Nullable ConfigEntry createLong(String namespace, Value<?> value, Component title, Runnable updateSaveState) {
		SliderResult slider = createLongSlider(title, (Long) value.value, value);
		if (slider != null) {
			return new ConfigEntry(namespace, title, slider.widget(), value, () -> value.accept(sliderLongValue(slider, value)), () -> true);
		}
		NotUglyEditBox box = createTextBox(title, String.valueOf(value.value), ConfigEntry::isLong, updateSaveState);
		return new ConfigEntry(namespace, title, box, value, () -> {
			if (isLong(box.getValue())) {
				value.accept(Long.parseLong(box.getValue()));
			}
		}, () -> isLong(box.getValue()));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static ConfigEntry createEnum(String namespace, Value<?> value, Component title, Class<?> type) {
		Enum<?>[] constants = (Enum<?>[]) type.getEnumConstants();
		CycleButton<Enum<?>> button = (CycleButton) CycleButton.builder(
						(Enum enumValue) -> enumValue instanceof LocalizableItem item ?
								item.getDisplayName().copy() :
								Component.literal(enumValue.name()),
						(Enum) value.value)
				.withValues((java.util.Collection) Arrays.asList(constants))
				.displayOnlyValue()
				.create(0, 0, 100, 20, title, (_, _) -> {});
		return new ConfigEntry(namespace, title, button, value, () -> value.accept(button.getValue()), () -> true);
	}

	private static ConfigEntry createStringList(String namespace, Value<?> value, Component title, Runnable updateSaveState) {
		String initial = String.join(", ", (List<String>) value.value);
		NotUglyEditBox box = createTextBox(title, initial, _ -> true, updateSaveState);
		return new ConfigEntry(namespace, title, box, value, () -> {
			List<String> list = Arrays.stream(box.getValue().split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.toList();
			value.accept(Lists.newArrayList(list));
		}, () -> true);
	}

	private static @Nullable SliderResult createIntSlider(Component title, int current, Value<?> value) {
		if (value.getAnnotation(Slider.class) == null || Double.isNaN(value.min) || Double.isNaN(value.max)) {
			return null;
		}
		int min = (int) value.min;
		int max = (int) value.max;
		double[] track = {normalized(current, min, max)};
		AbstractSliderButton slider = new AbstractSliderButton(0, 0, 100, 20, title, track[0]) {
			{
				updateMessage();
			}

			@Override
			protected void updateMessage() {
				setMessage(Component.literal(String.valueOf(Math.round(min + (max - min) * this.value))));
			}

			@Override
			protected void applyValue() {
				track[0] = this.value;
			}
		};
		return new SliderResult(slider, track);
	}

	private static @Nullable SliderResult createLongSlider(Component title, long current, Value<?> value) {
		if (value.getAnnotation(Slider.class) == null || Double.isNaN(value.min) || Double.isNaN(value.max)) {
			return null;
		}
		long min = (long) value.min;
		long max = (long) value.max;
		double[] track = {normalized(current, min, max)};
		AbstractSliderButton slider = new AbstractSliderButton(0, 0, 100, 20, title, track[0]) {
			{
				updateMessage();
			}

			@Override
			protected void updateMessage() {
				setMessage(Component.literal(String.valueOf(Math.round(min + (max - min) * this.value))));
			}

			@Override
			protected void applyValue() {
				track[0] = this.value;
			}
		};
		return new SliderResult(slider, track);
	}

	private static double normalized(double current, double min, double max) {
		if (max <= min) {
			return 0;
		}
		return Math.clamp((current - min) / (max - min), 0, 1);
	}

	private static int sliderIntValue(SliderResult slider, Value<?> value) {
		int min = (int) value.min;
		int max = (int) value.max;
		return (int) Math.round(min + (max - min) * slider.track()[0]);
	}

	private static long sliderLongValue(SliderResult slider, Value<?> value) {
		long min = (long) value.min;
		long max = (long) value.max;
		return Math.round(min + (max - min) * slider.track()[0]);
	}

	private static NotUglyEditBox createTextBox(
			Component title,
			String initial,
			Predicate<String> validator,
			Runnable updateSaveState) {
		NotUglyEditBox box = new NotUglyEditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 100, 18, title);
		box.setBordered(false);
		box.fixedTextX = 4;
		box.fixedTextY = 5;
		box.fixedInnerWidth = 92;
		box.setValue(initial);
		box.setResponder(text -> {
			boolean valid = validator.test(text);
			box.setTextColor(valid ? 0xFFFFFFFF : 0xFFFF5555);
			updateSaveState.run();
		});
		return box;
	}

	private static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isLong(String s) {
		try {
			Long.parseLong(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isFloat(String s) {
		try {
			Float.parseFloat(s.replace(',', '.'));
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isDouble(String s) {
		try {
			Double.parseDouble(s.replace(',', '.'));
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	private static boolean isColor(String s) {
		return s.matches("^#?[0-9a-fA-F]{6}$");
	}

	private static boolean isColorWithAlpha(String s) {
		return s.matches("^#?[0-9a-fA-F]{6}([0-9a-fA-F]{2})?$");
	}

	private static int parseColor(String s) {
		String value = s.startsWith("#") ? s.substring(1) : s;
		if (value.length() == 6) {
			return 0xFF000000 | Integer.parseInt(value, 16);
		}
		return (int) Long.parseLong(value, 16);
	}

	private static String colorToString(int color, boolean alpha) {
		return alpha ? String.format("#%08X", color) : String.format("#%06X", color & 0xFFFFFF);
	}

	@Override
	public boolean isValidValue() {
		return validator.getAsBoolean();
	}

	@Override
	public void save() {
		if (isValidValue()) {
			saver.run();
		}
	}

	@Override
	public void setValue(Object value) {
	}

	@Override
	public void updateValue() {
	}

	public Value<?> configValue() {
		return configValue;
	}

	public static MutableComponent rangeTooltip(Value<?> value) {
		String min = Double.isNaN(value.min) ? "-∞" :
				value.min == (long) value.min ? Long.toString((long) value.min) : Double.toString(value.min);
		String max = Double.isNaN(value.max) ? "+∞" :
				value.max == (long) value.max ? Long.toString((long) value.max) : Double.toString(value.max);
		return Component.translatable("gui.kiwi.config.range", min, max).withStyle(ChatFormatting.GRAY);
	}

	private record SliderResult(AbstractSliderButton widget, double[] track) {
	}
}

package snownee.kiwi.config;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Mth;
import snownee.kiwi.Kiwi;
import snownee.kiwi.KiwiModule.Skip;
import snownee.kiwi.config.KiwiConfig.AdvancedPath;
import snownee.kiwi.config.KiwiConfig.Comment;
import snownee.kiwi.config.KiwiConfig.ConfigType;
import snownee.kiwi.config.KiwiConfig.GameRestart;
import snownee.kiwi.config.KiwiConfig.LevelRestart;
import snownee.kiwi.config.KiwiConfig.Range;
import snownee.kiwi.config.KiwiConfig.Translation;

public class ConfigHandler {

	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	private boolean hasModules;
	private final String modId;
	private final String fileName;
	private final ConfigType type;
	@Nullable
	private final Class<?> clazz;
	private final Map<String, Value<?>> valueMap = Maps.newLinkedHashMap();
	Method onChanged;

	public static class Value<T> {
		String path;
		@Nullable
		public Field field;
		@NotNull
		public T value;
		@NotNull
		public final T defValue;
		public boolean requiresRestart;
		@Nullable
		public String[] comment;
		@Nullable
		public String translation;
		public double min = Double.NaN;
		public double max = Double.NaN;

		public Value(String path, @Nullable Field field, T value, String translation) {
			this.path = path;
			this.field = field;
			defValue = this.value = value;
			this.translation = translation;
		}

		public T get() {
			return value;
		}

		public Class<?> getType() {
			return field != null ? field.getType() : toPrimitiveClass(value.getClass());
		}

		public void accept(Object $, Method onChanged) {
			try {
				Class<?> type = getType();
				if (type == int.class) {
					int min = Double.isNaN(this.min) ? Integer.MIN_VALUE : (int) this.min;
					int max = Double.isNaN(this.max) ? Integer.MAX_VALUE : (int) this.max;
					if ($.getClass() == Long.class) {
						$ = ((Long) $).intValue();
					}
					$ = Integer.valueOf(Mth.clamp((Integer) $, min, max));
				} else if (type == float.class) {
					float min = Double.isNaN(this.min) ? Float.MIN_VALUE : (float) this.min;
					float max = Double.isNaN(this.max) ? Float.MAX_VALUE : (float) this.max;
					if ($.getClass() == Double.class) {
						$ = ((Double) $).floatValue();
					}
					$ = Float.valueOf(Mth.clamp((Float) $, min, max));
				} else if (type == double.class) {
					double min = Double.isNaN(this.min) ? Double.MIN_VALUE : this.min;
					double max = Double.isNaN(this.max) ? Double.MAX_VALUE : this.max;
					$ = Double.valueOf(Mth.clamp((Double) $, min, max));
				} else if (type == long.class) {
					long min = Double.isNaN(this.min) ? Long.MIN_VALUE : (long) this.min;
					long max = Double.isNaN(this.max) ? Long.MAX_VALUE : (long) this.max;
					$ = Long.valueOf(Mth.clamp((Long) $, min, max));
				}
				if (field != null) {
					if (type == boolean.class) {
						field.setBoolean(null, (Boolean) $);
					} else if (type == int.class) {
						field.setInt(null, (Integer) $);
					} else if (type == float.class) {
						field.setFloat(null, (Float) $);
					} else if (type == double.class) {
						field.setDouble(null, (Double) $);
					} else if (type == long.class) {
						field.setLong(null, (Long) $);
					} else {
						field.set(null, $);
					}
				}
				boolean changed = !Objects.equals(value, $);
				value = (T) $;
				if (changed && onChanged != null) {
					onChanged.invoke(null, path);
				}
			} catch (Exception e1) {
				Kiwi.logger.catching(e1);
			}
		}

		public <A extends Annotation> A getAnnotation(Class<A> clazz) {
			return field == null ? null : field.getAnnotation(clazz);
		}

	}

	public ConfigHandler(String modId, String fileName, ConfigType type, Class<?> clazz, boolean hasModules) {
		this.hasModules = hasModules;
		this.modId = modId;
		this.clazz = clazz;
		this.fileName = fileName;
		this.type = type;
		KiwiConfigManager.register(this);
		if (clazz != null) {
			try {
				onChanged = clazz.getDeclaredMethod("onChanged", String.class);
			} catch (Exception e) {
			}
		}
	}

	private Path getConfigPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(fileName + ".toml");
	}

	public void init() {
		build();
		Path configPath = getConfigPath();
		if (Files.exists(configPath)) {
			refresh();
		}
		save();
		//		try {
		//			Toml toml = new Toml().read(configPath.toFile());
		//		} catch (IllegalStateException e) {
		//			throw new SerializationException(e);
		//		}

		//		ConfigEntryBuilder builder = ConfigEntryBuilder.create();
		//		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		//		build(builder);
		//		ModContainer modContainer = ModList.get().getModContainerById(modId).orElseThrow(NullPointerException::new);
		//		config = new ModConfig(ModConfig.Type.valueOf(type.name()), builder.build(), modContainer, fileName);
		//		modContainer.addConfig(config);
		//		if (modContainer instanceof FMLModContainer) {
		//			((FMLModContainer) modContainer).getEventBus().addListener(this::onFileChange);
		//		}
	}

	@SuppressWarnings("rawtypes")
	public void refresh() {
		Path configPath = getConfigPath();
		Map<String, Object> map;
		try {
			Toml toml = new Toml().read(configPath.toFile());
			map = toml.toMap();
		} catch (IllegalStateException e) {
			save();
			return;
		}
		if (map.isEmpty()) {
			save();
			return;
		}
		Map<String, Object> flatMap = Maps.newHashMap();
		flatten(removeQuotes(map), flatMap, "", valueMap.keySet());
		for (Entry<String, Object> e : flatMap.entrySet()) {
			Value value = valueMap.get(e.getKey());
			if (value != null) {
				Object $ = e.getValue();
				Class<?> type = value.getType();
				if (Enum.class.isAssignableFrom(type)) {
					$ = EnumUtils.getEnumIgnoreCase((Class<Enum>) type, (String) $, (Enum) type.getEnumConstants()[0]);
				}
				value.accept($, onChanged);
			}
		}
	}

	// crude way to fix a bug in toml4j
	private static Map<String, Object> removeQuotes(Map<String, Object> map) {
		Map<String, Object> newMap = Maps.newHashMap();
		for (Entry<String, Object> e : map.entrySet()) {
			String key = e.getKey();
			if (key.startsWith("\"") && key.endsWith("\"")) {
				key = key.substring(1, key.length() - 1);
			}
			Object value = e.getValue();
			if (value instanceof Map) {
				value = removeQuotes((Map<String, Object>) value);
			}
			newMap.put(key, value);
		}
		return newMap;
	}

	private static void flatten(Map<String, Object> src, Map<String, Object> dst, String path, Set<String> keys) {
		for (Entry<String, Object> e : src.entrySet()) {
			String key = e.getKey();
			String newPath = path + key;
			if (e.getValue() instanceof Map && !keys.contains(newPath)) {
				flatten((Map<String, Object>) e.getValue(), dst, newPath + ".", keys);
			} else {
				dst.put(newPath, e.getValue());
			}
		}
	}

	public void save() {
		Path configPath = getConfigPath();
		Map<String, Object> map = Maps.newLinkedHashMap();
		for (Value<?> value : valueMap.values()) {
			List<String> path = List.of(value.path.split("\\."));
			Object v = value.field == null ? value.value : convert(value.field);
			value.accept(v, onChanged);
			getEndMap(map, path).put(path.get(path.size() - 1), v);
		}
		TomlWriter writer = new TomlWriter();
		try {
			writer.write(map, configPath.toFile());
		} catch (JsonSyntaxException | IOException e) {
			Kiwi.logger.catching(e);
		}
	}

	private void build() {
		if (hasModules) {
			KiwiConfigManager.defineModules(modId, this, !fileName.equals(modId + "-modules"));
		}
		if (clazz == null) {
			return;
		}
		Joiner joiner = Joiner.on('.');
		for (Field field : clazz.getFields()) {
			int mods = field.getModifiers();
			if (!Modifier.isPublic(mods) || !Modifier.isStatic(mods) || Modifier.isFinal(mods)) {
				continue;
			}
			if (field.getAnnotation(Skip.class) != null) {
				continue;
			}
			List<String> path = getPath(field);
			String pathKey = joiner.join(path);
			Translation translation = field.getAnnotation(Translation.class);
			String translationKey;
			if (translation != null) {
				translationKey = translation.value();
			} else {
				translationKey = pathKey;
			}
			Object converted = convert(field);
			if (converted == null) {
				continue;
			}
			Value<?> value = define(pathKey, converted, field, translationKey);
			if (field.getAnnotation(LevelRestart.class) != null || field.getAnnotation(GameRestart.class) != null) {
				// since there is no difference between these two options..
				value.requiresRestart = true;
			}
			Comment comment = field.getAnnotation(Comment.class);
			if (comment != null) {
				value.comment = comment.value();
			}
			Range range = field.getAnnotation(Range.class);
			if (range != null) {
				value.min = range.min();
				value.max = range.max();
			}
		}
	}

	/**
	 * Gets the path of a field: returns the annotated path, or the field's name if there is no
	 * annotated path.
	 *
	 * @return the annotated path, if any, or the field name
	 */
	static List<String> getPath(Field field) {
		List<String> annotatedPath = getPath((AnnotatedElement) field);
		return (annotatedPath == null) ? Collections.singletonList(field.getName()) : annotatedPath;
	}

	/**
	 * Gets the annotated path (specified with @Path or @AdvancedPath) of an annotated element.
	 *
	 * @return the annotated path, or {@code null} if there is none.
	 */
	static List<String> getPath(AnnotatedElement annotatedElement) {
		var path = annotatedElement.getDeclaredAnnotation(snownee.kiwi.config.KiwiConfig.Path.class);
		if (path != null) {
			return List.of(path.value().split("\\."));
		}
		AdvancedPath advancedPath = annotatedElement.getDeclaredAnnotation(AdvancedPath.class);
		if (advancedPath != null) {
			return Arrays.asList(advancedPath.value());
		}
		return null;
	}

	public <T> Value<T> define(String path, T value, @Nullable Field field, String translation) {
		Value<T> v = new Value<>(path, field, value, translation);
		valueMap.put(path, v);
		return v;
	}

	private static Map<String, Object> getEndMap(Map<String, Object> map, List<String> path) {
		int l = path.size() - 1;
		for (int i = 0; i < l; i++) {
			map = (Map<String, Object>) map.computeIfAbsent(path.get(i), $ -> Maps.newHashMap());
		}
		return map;
	}

	private static Object convert(Field field) {
		try {
			Class<?> type = field.getType();
			if (type != int.class && type != long.class && type != double.class && type != float.class && type != boolean.class && type != String.class && !Enum.class.isAssignableFrom(type) && !List.class.isAssignableFrom(type) && !Map.class.isAssignableFrom(type)) {
				return null;
			}
			if (type == String.class) {
				String defaultVal = (String) field.get(null);
				if (defaultVal == null) {
					defaultVal = "";
				}
				return defaultVal;
			}
			if (List.class.isAssignableFrom(type)) {
				List<?> defaultVal = (List<?>) field.get(null);
				if (defaultVal == null) {
					defaultVal = List.of();
				}
				return defaultVal;
			}
			if (Map.class.isAssignableFrom(type)) {
				Map<?, ?> defaultVal = (Map<?, ?>) field.get(null);
				if (defaultVal == null) {
					defaultVal = Map.of();
				}
				return defaultVal;
			}
			return field.get(null);
		} catch (Exception e) {
		}
		return null;
	}

	public void setHasModules(boolean hasModules) {
		this.hasModules = hasModules;
	}

	public boolean hasModules() {
		return hasModules;
	}

	public String getModId() {
		return modId;
	}

	public ConfigType getType() {
		return type;
	}

	public String getFileName() {
		return fileName;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	private static Class<?> toPrimitiveClass(Class<?> clazz) {
		if (clazz == Boolean.class) {
			return boolean.class;
		}
		if (clazz == Integer.class) {
			return int.class;
		}
		if (clazz == Long.class) {
			return long.class;
		}
		if (clazz == Float.class) {
			return float.class;
		}
		if (clazz == Double.class) {
			return double.class;
		}
		return clazz;
	}

	public <T> Value<T> get(String path) {
		return (Value<T>) valueMap.get(path);
	}

	public Map<String, Value<?>> getValueMap() {
		return valueMap;
	}

	//	static List<ResourceLocation> StrToIdList(List<String> list) {
	//		return list.stream().map(ResourceLocation::tryParse).filter(Objects::nonNull).toList();
	//	}
	//
	//	static List<String> IdToStrList(List<ResourceLocation> list) {
	//		return list.stream().map(Object::toString).toList();
	//	}
}

package snownee.kiwi.config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.SerializationException;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import snownee.kiwi.Kiwi;
import snownee.kiwi.config.KiwiConfig.Comment;
import snownee.kiwi.config.KiwiConfig.ConfigType;
import snownee.kiwi.config.KiwiConfig.LevelRestart;
import snownee.kiwi.config.KiwiConfig.Translation;

public class ConfigHandler {

	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	private boolean master;
	private final String modId;
	private final String fileName;
	private final ConfigType type;
	@Nullable
	private final Class<?> clazz;
	final Map<String, Value<?>> valueMap = Maps.newLinkedHashMap();
	Method onChanged;

	public static class Value<T> {
		String path;
		@Nullable
		public Field field;
		@NotNull
		public T value;
		public boolean requiresRestart;
		@Nullable
		public String[] comment;
		public Component component;

		public Value(String path, @Nullable Field field, T value, Component component) {
			this.path = path;
			this.field = field;
			this.value = value;
			this.component = component;
		}

		public T get() {
			return value;
		}

		public void accept(Object $, Method onChanged) {
			try {
				if (!requiresRestart && field != null) {
					Class<?> type = field.getType();
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
						field.set(null, field.getType().cast($));
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

	}

	public ConfigHandler(String modId, String fileName, ConfigType type, Class<?> clazz, boolean master) {
		this.master = master;
		this.modId = modId;
		this.clazz = clazz;
		this.fileName = fileName;
		this.type = type;
		build();
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
		Path configPath = getConfigPath();
		if (Files.exists(configPath)) {
			refresh();
		}
		save();
		try {
			Toml toml = new Toml().read(configPath.toFile());
		} catch (IllegalStateException e) {
			throw new SerializationException(e);
		}
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
		flat(map, flatMap, "");
		for (Entry<String, Object> e : flatMap.entrySet()) {
			Value value = valueMap.get(e.getKey());
			if (value != null) {
				value.accept(e.getValue(), onChanged);
			}
		}
	}

	private static void flat(Map<String, Object> src, Map<String, Object> dst, String path) {
		for (Entry<String, Object> e : src.entrySet()) {
			if (e.getValue() instanceof Map) {
				flat((Map<String, Object>) e.getValue(), dst, path + e.getKey() + ".");
			} else {
				dst.put(path + e.getKey(), e.getValue());
			}
		}
	}

	public void save() {
		Path configPath = getConfigPath();
		Map<String, Object> map = Maps.newLinkedHashMap();
		for (Value<?> value : valueMap.values()) {
			List<String> path = List.of(value.path.split("\\."));
			getEndMap(map, path).put(path.get(path.size() - 1), value.field == null ? value.value : convert(value.field));
		}
		TomlWriter writer = new TomlWriter();
		try {
			writer.write(map, configPath.toFile());
		} catch (JsonSyntaxException | IOException e) {
			Kiwi.logger.catching(e);
		}
	}

	//TODO refactor
	private void build() {
		if (master) {
			KiwiConfigManager.defineModules(modId, this);
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
			List<String> path = NightConfigUtil.getPath(field);
			Translation translation = field.getAnnotation(Translation.class);
			Component component;
			if (translation != null) {
				component = new TranslatableComponent(modId + ".config." + translation.value());
			} else {
				component = new TextComponent(path.get(path.size() - 1));
			}
			Value<?> value = define(joiner.join(path), convert(field), field, component);
			if (field.getAnnotation(LevelRestart.class) != null) {
				value.requiresRestart = true;
			}
			Comment comment = field.getAnnotation(Comment.class);
			if (comment != null) {
				value.comment = comment.value();
			}
		}
	}

	public <T> Value<T> define(String path, T value, @Nullable Field field, Component component) {
		Value<T> v = new Value<>(path, field, value, component);
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
			if (type != int.class && type != long.class && type != double.class && type != float.class && type != boolean.class && type != String.class && !Enum.class.isAssignableFrom(type) && !List.class.isAssignableFrom(type)) {
				return null;
			}
			if (type == String.class) {
				String defaultVal = (String) field.get(null);
				if (defaultVal == null) {
					defaultVal = "";
				}
				return defaultVal;
			}
			if (Enum.class.isAssignableFrom(type)) {
				return Objects.toString(field.get(null));
			}
			if (List.class.isAssignableFrom(type)) {
				List<?> defaultVal = (List<?>) field.get(null);
				if (defaultVal == null) {
					defaultVal = List.of();
				}
				return defaultVal;
			}
			return field.get(null);
		} catch (Exception e) {
		}
		return null;
	}

	//	public void refresh() {
	//		valueMap.forEach((field, value) -> {
	//			try {
	//				if (field.getType() == float.class) {
	//					if (Objects.equals(((Double) value.get()).floatValue(), field.get(null))) {
	//						return;
	//					}
	//					field.setFloat(null, ((Double) value.get()).floatValue());
	//				} else {
	//					if (field.getType() != List.class && Objects.deepEquals(value.get(), field.get(null))) {
	//						return;
	//					}
	//					field.set(null, value.get());
	//				}
	//				Kiwi.logger.debug("Set " + field.getName() + " to " + value.get());
	//				if (onChanged != null)
	//					onChanged.invoke(null, Joiner.on('.').join(value.getPath()));
	//			} catch (Exception e) {
	//				Kiwi.logger.catching(e);
	//			}
	//		});
	//	}

	//	public void forceLoad() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
	//		java.nio.file.Path path;
	//		if (type == ConfigType.SERVER) {
	//			path = Platform.getServer().getFile("serverconfig").toPath();
	//		} else {
	//			path = FMLPaths.CONFIGDIR.get();
	//		}
	//		CommentedFileConfig configData = config.getHandler().reader(path).apply(config);
	//		Field fCfg = ModConfig.class.getDeclaredField("configData");
	//		fCfg.setAccessible(true);
	//		fCfg.set(config, configData);
	//		config.getSpec().acceptConfig(configData);
	//		config.getHandler().unload(path, config);
	//		//config.save();
	//	}

	//	protected void onFileChange(ModConfigEvent.Reloading event) {
	//		if (event.getConfig() == config) {
	//			((CommentedFileConfig) event.getConfig().getConfigData()).load();
	//			refresh();
	//		}
	//	}

	public void setMaster(boolean master) {
		this.master = master;
	}

	public boolean isMaster() {
		return master;
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

	//	public ConfigValue<?> getValueByPath(String path) {
	//		Joiner joiner = Joiner.on(".");
	//		for (ConfigValue<?> value : valueMap.values()) {
	//			if (path.equals(joiner.join(value.getPath()))) {
	//				return value;
	//			}
	//		}
	//		return null;
	//	}
}

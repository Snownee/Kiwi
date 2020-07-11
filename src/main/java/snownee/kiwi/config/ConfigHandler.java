package snownee.kiwi.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.Predicates;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import snownee.kiwi.Kiwi;
import snownee.kiwi.config.KiwiConfig.Comment;
import snownee.kiwi.config.KiwiConfig.Range;
import snownee.kiwi.config.KiwiConfig.Translation;
import snownee.kiwi.config.KiwiConfig.WorldRestart;

public class ConfigHandler {

    private boolean master;
    private final String modId;
    private final String fileName;
    private final ModConfig.Type type;
    @Nullable
    private final Class<?> clazz;
    private final BiMap<Field, ConfigValue<?>> valueMap = HashBiMap.create();

    public ConfigHandler(String modId, String fileName, ModConfig.Type type, Class<?> clazz, boolean master) {
        this.master = master;
        this.modId = modId;
        this.clazz = clazz;
        this.fileName = fileName;
        this.type = type;
    }

    public void init() {
        Pair<ConfigHandler, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(this::build);
        ModContainer modContainer = ModList.get().getModContainerById(modId).orElseThrow(NullPointerException::new);
        ModConfig modConfig = new ModConfig(type, specPair.getRight(), modContainer, fileName);
        modContainer.addConfig(modConfig);
        if (modContainer instanceof FMLModContainer) {
            ((FMLModContainer) modContainer).getEventBus().addListener(this::onFileChange);
        }
    }

    private ConfigHandler build(ForgeConfigSpec.Builder builder) {
        if (master) {
            KiwiConfigManager.defineModules(modId, builder);
        }
        if (clazz == null) { //TODO config that only contains module toggles
            return this;
        }
        for (Field field : clazz.getFields()) {
            int mods = field.getModifiers();
            if (!Modifier.isPublic(mods) || !Modifier.isStatic(mods) || Modifier.isFinal(mods)) {
                continue;
            }
            Class<?> type = field.getType();
            if (type != int.class && type != long.class && type != double.class && type != boolean.class && type != String.class && !Enum.class.isAssignableFrom(type) && !List.class.isAssignableFrom(type)) {
                continue;
            }
            if (field.getAnnotation(WorldRestart.class) != null) {
                builder.worldRestart();
            }
            Comment comment = field.getAnnotation(Comment.class);
            if (comment != null) {
                builder.comment(comment.value());
            }
            Translation translation = field.getAnnotation(Translation.class);
            if (translation != null) {
                builder.translation(translation.value());
            }
            String path;
            Path pathAnnotation = field.getAnnotation(Path.class);
            if (pathAnnotation == null) {
                path = field.getName();
            } else {
                path = pathAnnotation.value();
            }
            ConfigValue<?> value = null;
            try {
                if (type == int.class || type == long.class || type == double.class) {
                    double min = Double.NaN;
                    double max = Double.NaN;
                    Range range = field.getAnnotation(Range.class);
                    if (range != null) {
                        min = range.min();
                        max = range.max();
                    }
                    if (type == int.class) {
                        value = builder.defineInRange(path, field.getInt(null), Double.isNaN(min) ? Integer.MIN_VALUE : (int) min, Double.isNaN(max) ? Integer.MAX_VALUE : (int) max);
                    } else if (type == long.class) {
                        value = builder.defineInRange(path, field.getLong(null), Double.isNaN(min) ? Long.MIN_VALUE : (long) min, Double.isNaN(max) ? Long.MAX_VALUE : (long) max);
                    } else if (type == double.class) {
                        value = builder.defineInRange(path, field.getDouble(null), Double.isNaN(min) ? Double.MIN_VALUE : min, Double.isNaN(max) ? Double.MAX_VALUE : max);
                    }
                } else if (type == String.class) {
                    value = builder.define(path, field.get(null));
                } else if (type == boolean.class) {
                    value = builder.define(path, field.getBoolean(null));
                } else if (Enum.class.isAssignableFrom(type)) {
                    value = builder.defineEnum(path, (Enum) field.get(null));
                } else if (List.class.isAssignableFrom(type)) {
                    value = builder.defineList(path, (List) field.get(null), Predicates.alwaysTrue());
                }
                valueMap.put(field, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                Kiwi.logger.catching(e);
            }
        }
        return this;
    }

    public void refresh() {
        valueMap.forEach((field, value) -> {
            try {
                Kiwi.logger.debug("Set " + field.getName() + " to " + value.get());
                field.set(null, value.get());
            } catch (IllegalArgumentException | IllegalAccessException e) {
                Kiwi.logger.catching(e);
            }
        });
    }

    @SubscribeEvent
    protected void onFileChange(ModConfig.Reloading event) {
        ((CommentedFileConfig) event.getConfig().getConfigData()).load();
        refresh();
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public boolean isMaster() {
        return master;
    }

    public String getModId() {
        return modId;
    }

    public ModConfig.Type getType() {
        return type;
    }

    public String getFileName() {
        return fileName;
    }
}

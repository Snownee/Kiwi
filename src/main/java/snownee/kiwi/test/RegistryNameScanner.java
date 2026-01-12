package snownee.kiwi.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import snownee.kiwi.Kiwi;

public class RegistryNameScanner {
	public static void run() throws Exception {
		StringBuilder sb = new StringBuilder();
		List<Class<?>> registryClasses = scanStaticFields(BuiltInRegistries.class, Registry.class).stream()
				.map(matchGeneric(Pattern.compile("<L([^<;]+?)[<;]")))
				.filter(Objects::nonNull)
				.toList();
		List<FieldNode> fields = scanStaticFields(Registries.class, ResourceKey.class);

		Function<FieldNode, Class<?>> func = matchGeneric(Pattern.compile("Lnet/minecraft/core/Registry<L([^<;]+?)[<;]"));
		for (FieldNode field : fields) {
			Class<?> registryClass = func.apply(field);
			if (!registryClasses.contains(registryClass)) {
				continue;
			}
			if (registryClass == Identifier.class || registryClass == MapCodec.class) {
				continue;
			}
			String name = registryClass.getName();
			name = name.substring(name.lastIndexOf('.') + 1).replace("$", ".");
			sb.append("registerRegistry(Registries.%s, %s.class);\n".formatted(field.name, name));
		}
		Kiwi.LOGGER.info(sb.toString());
	}

	private static Function<FieldNode, @Nullable Class<?>> matchGeneric(Pattern pattern) {
		return fieldNode -> {
			Matcher matcher = pattern.matcher(fieldNode.signature);
			if (!matcher.find()) {
				return null;
			}
			String className = matcher.group(1).replace('/', '.');
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Failed to load class: " + className, e);
			}
		};
	}

	private static List<FieldNode> scanStaticFields(Class<?> clazz, Class<?> fieldType) {
		List<FieldNode> result = new ArrayList<>();
		String classPath = clazz.getName().replace('.', '/') + ".class";
		ClassLoader classLoader = clazz.getClassLoader();
		try (InputStream is = classLoader.getResourceAsStream(classPath)) {
			ClassReader classReader = new ClassReader(Objects.requireNonNull(is));
			ClassNode classNode = new ClassNode();
			classReader.accept(classNode, 0);
			for (FieldNode fieldNode : classNode.fields) {
				if (!isPublicStatic(fieldNode)) {
					continue;
				}
				Class<?> fieldClass = getFieldClass(fieldNode, classLoader);
				if (fieldType.isAssignableFrom(fieldClass)) {
					result.add(fieldNode);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to scan class: " + clazz.getName(), e);
		}
		return result;
	}

	private static boolean isPublicStatic(FieldNode fieldNode) {
		// 检查是否包含 ACC_PUBLIC 且包含 ACC_STATIC
		return (fieldNode.access & Opcodes.ACC_PUBLIC) != 0 &&
				(fieldNode.access & Opcodes.ACC_STATIC) != 0;
	}

	private static boolean isPrimitive(Type type) {
		int sort = type.getSort();
		return sort >= Type.BOOLEAN && sort <= Type.DOUBLE;
	}

	private static Class<?> getFieldClass(FieldNode fieldNode, ClassLoader loader) throws ClassNotFoundException {
		// 1. 获取 Type 对象
		Type type = Type.getType(fieldNode.desc);

		// 2. 处理基本类型 (int, boolean, etc.)
		if (isPrimitive(type)) {
			throw new IllegalArgumentException("Field is of primitive type: " + fieldNode.name);
		}

		// 3. 获取类名并加载
		// getClassName() 会自动处理数组（如 [Ljava/lang/String; -> java.lang.String[]）
		String className = type.getClassName();
		return Class.forName(className, false, loader);
	}
}

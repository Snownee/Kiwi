package snownee.kiwi.config;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import snownee.kiwi.config.KiwiConfig.AdvancedPath;
import snownee.kiwi.config.KiwiConfig.Path;

public class NightConfigUtil {

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
		Path path = annotatedElement.getDeclaredAnnotation(Path.class);
		if (path != null) {
			return List.of(path.value().split("\\."));
		}
		AdvancedPath advancedPath = annotatedElement.getDeclaredAnnotation(AdvancedPath.class);
		if (advancedPath != null) {
			return Arrays.asList(advancedPath.value());
		}
		return null;
	}

	//	static Predicate<Object> getValidator(Field field) {
	//		final Predicate<Object> validatorInstance;
	//		try {
	//			SpecValidator spec = field.getAnnotation(SpecValidator.class);
	//			if (spec == null) {
	//				return Predicates.alwaysTrue();
	//			}
	//			Constructor<? extends Predicate<Object>> constructor = spec.value().getDeclaredConstructor();
	//			constructor.setAccessible(true);
	//			validatorInstance = constructor.newInstance();
	//		} catch (ReflectiveOperationException ex) {
	//			throw new ReflectionException("Cannot create a converter for field " + field, ex);
	//		}
	//		return validatorInstance;
	//	}
}

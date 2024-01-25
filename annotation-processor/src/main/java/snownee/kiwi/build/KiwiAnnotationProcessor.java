package snownee.kiwi.build;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.gson.GsonBuilder;

import snownee.kiwi.KiwiAnnotationData;

/* off */
@SupportedAnnotationTypes({
		"snownee.kiwi.KiwiModule",
		"snownee.kiwi.KiwiModule.Optional",
		"snownee.kiwi.KiwiModule.LoadingCondition",
		"snownee.kiwi.config.KiwiConfig",
		"snownee.kiwi.network.KiwiPacket",
		"snownee.kiwi.Mod",
})
@SuppressWarnings(value = {"unchecked"})
/* on */
public class KiwiAnnotationProcessor extends AbstractProcessor {

	Messager messager;
	Filer filer;
	String modId;
	Elements elementUtils;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		messager = processingEnv.getMessager();
		filer = processingEnv.getFiler();
		elementUtils = processingEnv.getElementUtils();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (annotations.isEmpty()) {
			return true;
		}
		messager.printMessage(Kind.NOTE, "KiwiAnnotationProcessor is processing");
		TreeMap<String, List<KiwiAnnotationData>> map = new TreeMap<>();
		for (TypeElement annotation : annotations) {
			String className = annotation.toString();
			ElementKind elementKind = ElementKind.CLASS;
			if ("snownee.kiwi.KiwiModule.Optional".equals(className)) {
				className = "snownee.kiwi.KiwiModule$Optional";
			} else if ("snownee.kiwi.KiwiModule.LoadingCondition".equals(className)) {
				className = "snownee.kiwi.KiwiModule$LoadingCondition";
				elementKind = ElementKind.METHOD;
			}
			Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
			for (Element ele : elements) {
				if (ele.getKind() != elementKind) {
					messager.printMessage(Kind.ERROR, "Annotated element is not matched", ele);
					continue;
				}
				AnnotationMirror a = getAnnotation(ele, annotation);
				Map<String, Object> o = new TreeMap<>();
				for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : a.getElementValues().entrySet()) {
					o.put(e.getKey().getSimpleName().toString(), mapValue(e.getValue()));
				}
				if ("snownee.kiwi.Mod".equals(className)) {
					if (modId == null) {
						modId = (String) o.get("value");
					} else {
						messager.printMessage(Kind.ERROR, "Found more than one @Mod");
					}
					continue;
				}
				String target;
				if (elementKind == ElementKind.METHOD) {
					target = ele.getEnclosingElement().toString();
					o.put("method", ele.getSimpleName().toString());
				} else {
					target = ele.toString();
				}
				if (!target.startsWith("snownee.kiwi.test."))
					map.computeIfAbsent(annotation.getSimpleName().toString(), $ -> new ArrayList<>()).add(new KiwiAnnotationData(target, o.isEmpty() ? null : o));
			}
		}
		String json = new GsonBuilder().setPrettyPrinting().create().toJson(map);
		//		messager.printMessage(Kind.NOTE, json);

		try {
			FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", modId + ".kiwi.json");
			try (PrintWriter writer = new PrintWriter(file.openWriter())) {
				writer.write(json);
			}
		} catch (IOException e) {
			messager.printMessage(Kind.ERROR, e.toString());
		}
		return true;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	// modified from Mixin
	private static AnnotationMirror getAnnotation(Element elem, TypeElement annotation2) {
		if (elem == null) {
			return null;
		}

		List<? extends AnnotationMirror> annotations = elem.getAnnotationMirrors();

		if (annotations == null) {
			return null;
		}

		for (AnnotationMirror annotation : annotations) {
			Element element = annotation.getAnnotationType().asElement();
			if (!(element instanceof TypeElement annotationElement)) {
				continue;
			}
			if (annotationElement.equals(annotation2)) {
				return annotation;
			}
		}
		return null;
	}

	private static Object mapValue(AnnotationValue av) {
		Object v = av.getValue();
		if (v instanceof VariableElement) {
			v = v.toString();
		} else if (v instanceof List) {
			v = ((List<AnnotationValue>) v).stream().map(KiwiAnnotationProcessor::mapValue).toList();
		}
		return v;
	}

}

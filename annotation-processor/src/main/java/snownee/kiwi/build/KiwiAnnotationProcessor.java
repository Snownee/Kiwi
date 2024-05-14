package snownee.kiwi.build;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.io.Closeables;
import com.google.gson.GsonBuilder;

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
import snownee.kiwi.KiwiAnnotationData;

@SupportedAnnotationTypes(
		{
				"snownee.kiwi.KiwiModule",
				"snownee.kiwi.KiwiModule.Optional",
				"snownee.kiwi.KiwiModule.LoadingCondition",
				"snownee.kiwi.config.KiwiConfig",
				"snownee.kiwi.network.KiwiPacket",
				"net.minecraftforge.fml.common.Mod",
		})
@SuppressWarnings(value = {"unchecked"})
public class KiwiAnnotationProcessor extends AbstractProcessor {

	Messager messager;
	Filer filer;
	String modId;
	Elements elementUtils;
	TypeElement skipType;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		messager = processingEnv.getMessager();
		filer = processingEnv.getFiler();
		elementUtils = processingEnv.getElementUtils();
		skipType = processingEnv.getElementUtils().getTypeElement("snownee.kiwi.KiwiModule.Skip");
		Objects.requireNonNull(skipType);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (annotations.isEmpty()) {
			return true;
		}
		messager.printMessage(Kind.NOTE, "KiwiAnnotationProcessor is processing");
		Multimap<String, KiwiAnnotationData> map = Multimaps.newMultimap(Maps.newTreeMap(), Lists::newArrayList);
		for (TypeElement annotation : annotations) {
			String className = annotation.getQualifiedName().toString();
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
				if (a == null) {
					continue;
				}
				Map<String, Object> o = Maps.newHashMap();
				for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : a.getElementValues().entrySet()) {
					o.put(e.getKey().getSimpleName().toString(), mapValue(e.getValue()));
				}
				if ("net.minecraftforge.fml.common.Mod".equals(className)) {
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
				if (!target.startsWith("snownee.kiwi.test.")) {
					map.put(annotation.getSimpleName().toString(), new KiwiAnnotationData(target, o.isEmpty() ? null : o));
				}
			}
		}
		String json = new GsonBuilder().setPrettyPrinting().create().toJson(map.asMap());
		//		messager.printMessage(Kind.NOTE, json);

		PrintWriter writer = null;
		try {
			FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", modId + ".kiwi.json");
			writer = new PrintWriter(file.openWriter());
			writer.write(json);
		} catch (IOException e) {
			messager.printMessage(Kind.ERROR, e.toString());
		} finally {
			try {
				Closeables.close(writer, true);
			} catch (IOException e) {
			}
		}
		return true;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	// modified from Mixin
	private AnnotationMirror getAnnotation(Element elem, TypeElement annotation2) {
		if (elem == null) {
			return null;
		}

		List<? extends AnnotationMirror> annotations = elem.getAnnotationMirrors();

		if (annotations == null) {
			return null;
		}

		AnnotationMirror found = null;
		for (AnnotationMirror annotation : annotations) {
			Element element = annotation.getAnnotationType().asElement();
			if (!(element instanceof TypeElement annotationElement)) {
				continue;
			}
			if (annotationElement.equals(skipType)) {
				return null;
			}
			if (annotationElement.equals(annotation2)) {
				found = annotation;
			}
		}
		return found;
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
package snownee.kiwi.build;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
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
				KiwiAnnotationProcessor.MOD_ANNOTATION})
@SupportedOptions(
		{
				"kiwi.clientOnlyMod",
				"kiwi.projectModId"})
@SuppressWarnings({"unchecked"})
public class KiwiAnnotationProcessor extends AbstractProcessor {

	public static final String MOD_ANNOTATION = "net.neoforged.fml.common.Mod";

	Messager messager;
	Filer filer;
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
		Messager messager = processingEnv.getMessager();
		messager.printMessage(Kind.NOTE, "KiwiAnnotationProcessor is processing");
		KiwiMetadata metadata = new KiwiMetadata(processingEnv.getOptions().containsKey("kiwi.clientOnlyMod"));
		String modId = null;
		String optionModId = processingEnv.getOptions().get("kiwi.projectModId");
		for (TypeElement annotation : annotations) {
			String className = annotation.getQualifiedName().toString();
			AnnotationType type = AnnotationType.MAP.get(className);
			Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
			for (Element element : elements) {
				if (!type.isCorrectKind(element, messager)) {
					messager.printMessage(Kind.ERROR, "Annotated element is not matched", element);
					continue;
				}
				AnnotationMirror a = getAnnotation(element, annotation);
				if (a == null) {
					continue;
				}
				Map<String, Object> o = new TreeMap<>();
				for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : a.getElementValues().entrySet()) {
					o.put(e.getKey().getSimpleName().toString(), mapValue(e.getValue()));
				}
				if (type == AnnotationType.MOD && optionModId == null) {
					String value = (String) o.get("value");
					if (modId == null) {
						modId = value;
					} else if (!modId.equals(value)) {
						messager.printMessage(
								Kind.ERROR,
								"Found @Mod annotations with different mod ids, please specify mod id with the \"kiwi.projectModId\" processor option",
								element);
					}
					continue;
				}
				String target;
				if (type.allowedKinds.contains(ElementKind.METHOD)) {
					target = element.getEnclosingElement().toString();
					o.put("method", element.getSimpleName().toString());
				} else {
					target = element.toString();
				}
				KiwiAnnotationData value = new KiwiAnnotationData();
				value.setTarget(target);
				value.setData(o);
				metadata.map().computeIfAbsent(type.yamlKey, $ -> new ArrayList<>()).add(value);
			}
		}

		if (metadata.map().isEmpty() && !metadata.clientOnly()) {
			return true;
		}
		metadata.map().values().forEach(list -> list.sort(Comparator.comparing(KiwiAnnotationData::getTarget)));

		String yaml = new KiwiMetadataParser().dump(metadata);
//		new KiwiMetadataParser().load(yaml);
//		messager.printMessage(Kind.NOTE, yaml);

		if (modId == null) {
			modId = optionModId;
			if (modId == null) {
				messager.printMessage(Kind.ERROR, "No mod id found, use @Mod", null);
			}
		}
		try {
			FileObject file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", modId + ".kiwi.yaml");
			try (PrintWriter writer = new PrintWriter(file.openWriter())) {
				writer.write(yaml);
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

	private record AnnotationType(String className, String yamlKey, Set<ElementKind> allowedKinds) {
		private static final AnnotationType MODULE = new AnnotationType("snownee.kiwi.KiwiModule", "modules", Set.of(ElementKind.CLASS));
		private static final AnnotationType OPTIONAL = new AnnotationType(
				"snownee.kiwi.KiwiModule.Optional",
				"optionals",
				Set.of(ElementKind.CLASS));
		private static final AnnotationType LOADING_CONDITION = new AnnotationType(
				"snownee.kiwi.KiwiModule.LoadingCondition",
				"conditions",
				Set.of(ElementKind.METHOD));
		private static final AnnotationType CONFIG = new AnnotationType(
				"snownee.kiwi.config.KiwiConfig",
				"configs",
				Set.of(ElementKind.CLASS));
		private static final AnnotationType PACKET = new AnnotationType(
				"snownee.kiwi.network.KiwiPacket",
				"packets",
				Set.of(ElementKind.RECORD, ElementKind.CLASS));
		private static final AnnotationType MOD = new AnnotationType(MOD_ANNOTATION, "mod", Set.of());
		private static final Map<String, AnnotationType> MAP = Stream.of(MODULE, OPTIONAL, LOADING_CONDITION, CONFIG, PACKET, MOD).collect(
				HashMap::new, (m, t) -> m.put(t.className, t), Map::putAll);

		boolean isCorrectKind(Element element, Messager messager) {
			if (!allowedKinds.isEmpty() && !allowedKinds.contains(element.getKind())) {
				messager.printMessage(Kind.ERROR, "Annotated element is not matched to expected kind", element);
				return false;
			}
			return true;
		}
	}

}

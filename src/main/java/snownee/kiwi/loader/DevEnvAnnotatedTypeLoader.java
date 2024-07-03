package snownee.kiwi.loader;

import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.Type;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.modscan.ModAnnotation;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import snownee.kiwi.KiwiAnnotationData;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.config.KiwiConfig;
import snownee.kiwi.network.KiwiPacket;

public class DevEnvAnnotatedTypeLoader extends AnnotatedTypeLoader {

	public DevEnvAnnotatedTypeLoader(String modId) {
		super(modId);
	}

	@Override
	public KiwiConfiguration get() {
		IModFileInfo modFileInfo = ModList.get().getModFileById(modId);
		if (modFileInfo == null) {
			return null;
		}

		final Type KIWI_MODULE = Type.getType(KiwiModule.class);
		final Type KIWI_CONFIG = Type.getType(KiwiConfig.class);
		final Type KIWI_PACKET = Type.getType(KiwiPacket.class);
		final Type OPTIONAL_MODULE = Type.getType(KiwiModule.Optional.class);
		final Type LOADING_CONDITION = Type.getType(KiwiModule.LoadingCondition.class);

		KiwiConfiguration configuration = new KiwiConfiguration();
		configuration.conditions = Lists.newArrayList();
		configuration.optionals = Lists.newArrayList();
		configuration.modules = Lists.newArrayList();
		configuration.packets = Lists.newArrayList();
		configuration.configs = Lists.newArrayList();

		for (ModFileScanData.AnnotationData annotationData : modFileInfo.getFile().getScanResult().getAnnotations()) {
			Type annotationType = annotationData.annotationType();
			if (KIWI_MODULE.equals(annotationType)) {
				configuration.modules.add(map(annotationData));
			} else if (KIWI_CONFIG.equals(annotationData.annotationType())) {
				configuration.configs.add(map(annotationData));
			} else if (OPTIONAL_MODULE.equals(annotationType)) {
				configuration.optionals.add(map(annotationData));
			} else if (LOADING_CONDITION.equals(annotationType)) {
				KiwiAnnotationData mapped = map(annotationData);
				String methodName = annotationData.memberName();
				int p = methodName.indexOf('(');
				if (p <= 0) {
					throw new IllegalArgumentException();
				}
				methodName = methodName.substring(0, p);
				mapped.getData().put("method", methodName);
				configuration.conditions.add(mapped);
			} else if (KIWI_PACKET.equals(annotationType)) {
				configuration.packets.add(map(annotationData));
			}
		}
		return configuration;
	}

	private static KiwiAnnotationData map(ModFileScanData.AnnotationData data) {
		Map<String, Object> annotationData = Maps.newHashMap();
		for (Entry<String, Object> e : data.annotationData().entrySet()) {
			if (e.getValue() instanceof ModAnnotation.EnumHolder) {
				annotationData.put(e.getKey(), ((ModAnnotation.EnumHolder) e.getValue()).value());
			} else {
				annotationData.put(e.getKey(), e.getValue());
			}
		}
		KiwiAnnotationData kiwiAnnotationData = new KiwiAnnotationData();
		kiwiAnnotationData.setTarget(data.clazz().getClassName());
		kiwiAnnotationData.setData(annotationData);
		return kiwiAnnotationData;
	}
}

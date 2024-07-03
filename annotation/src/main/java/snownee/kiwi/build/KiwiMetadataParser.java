package snownee.kiwi.build;

import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import snownee.kiwi.KiwiAnnotationData;

public class KiwiMetadataParser {
	private final Yaml yaml;

	public KiwiMetadataParser() {
		Representer representer = new Representer(new DumperOptions());
		TypeDescription typeDescription = new TypeDescription(KiwiAnnotationData.class, Tag.MAP);
		representer.addTypeDescription(typeDescription);
		Constructor constructor = new Constructor(new LoaderOptions(), typeDescription);
		yaml = new Yaml(constructor, representer);
	}

	public String dump(KiwiMetadata metadata) {
		TreeMap<String, Object> map = new TreeMap<>(metadata.map());
		map.put("clientOnly", metadata.clientOnly());
		return yaml.dump(map);
	}

	public KiwiMetadata load(InputStream is) {
		return KiwiMetadata.of(yaml.loadAs(is, Map.class));
	}

	public KiwiMetadata load(String s) {
		return KiwiMetadata.of(yaml.loadAs(s, Map.class));
	}

	private static class Constructor extends org.yaml.snakeyaml.constructor.Constructor {
		public Constructor(LoaderOptions loaderOptions, TypeDescription typeDescription) {
			super(loaderOptions);
			typeDefinitions.put(typeDescription.getType(), typeDescription);
		}

		@Override
		public Object getSingleData(Class<?> type) {
			// Ensure that the stream contains a single document and construct it
			final Node node = composer.getSingleNode();
			if (node != null && !Tag.NULL.equals(node.getTag())) {
				if (Object.class != type) {
					node.setTag(new Tag(type));
				} else if (rootTag != null) {
					node.setTag(rootTag);
				}
				if (node instanceof MappingNode mappingNode) {
					for (NodeTuple nodeTuple : mappingNode.getValue()) {
						if (nodeTuple.getValueNode() instanceof SequenceNode sequenceNode) {
							for (Node child : sequenceNode.getValue()) {
								if (child instanceof MappingNode) {
									child.setType(KiwiAnnotationData.class);
								}
							}
						}
					}
				}
				return constructDocument(node);
			} else {
				Construct construct = yamlConstructors.get(Tag.NULL);
				return construct.construct(node);
			}
		}
	}
}

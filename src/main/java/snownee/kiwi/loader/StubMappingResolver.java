package snownee.kiwi.loader;

import snownee.kiwi.util.resource.MappingResolver;

public class StubMappingResolver implements MappingResolver {
	@Override
	public String unmapClass(String clazz) {
		return clazz;
	}
}

package snownee.kiwi.lang;

import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record LangRule(List<String> prefixes, Map<String, String> define) {
	public static final Codec<List<String>> PREFIXES_CODEC = Codec.withAlternative(Codec.STRING.listOf(), Codec.STRING, prefix -> List.of(prefix));
	public static final Codec<LangRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PREFIXES_CODEC.fieldOf("prefixes").forGetter(LangRule::prefixes),
			Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("define", Map.of()).forGetter(LangRule::define)
	).apply(instance, LangRule::new));
}

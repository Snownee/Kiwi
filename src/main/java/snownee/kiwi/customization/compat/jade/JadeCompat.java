package snownee.kiwi.customization.compat.jade;

/**
 * Jade's current NeoForge artifact still exposes `ResourceLocation`-typed provider APIs,
 * which do not remap cleanly in this `Identifier`-mapped workspace yet.
 * Keep this compat entry disabled until the API artifact or mappings are aligned.
 */
public final class JadeCompat {
	private JadeCompat() {
	}
}

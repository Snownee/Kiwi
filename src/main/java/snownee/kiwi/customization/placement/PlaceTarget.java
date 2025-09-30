package snownee.kiwi.customization.placement;

import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public record PlaceTarget(Type type, ResourceLocation id) {
	public static final Codec<PlaceTarget> CODEC = ExtraCodecs.NON_EMPTY_STRING.xmap(PlaceTarget::of, PlaceTarget::toString);

	public static PlaceTarget of(String s) {
		Type type = s.startsWith("@") ? Type.TEMPLATE : Type.BLOCK;
		ResourceLocation id = ResourceLocation.parse(s.substring(type.prefix.length()));
		return new PlaceTarget(type, id);
	}

	@Override
	public String toString() {
		return type.prefix + id;
	}

	public enum Type {
		TEMPLATE("@"),
		BLOCK("");

		public final String prefix;

		Type(String prefix) {
			this.prefix = prefix;
		}
	}
}

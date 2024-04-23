package snownee.kiwi.customization.placement;

import java.util.Locale;

import snownee.kiwi.customization.block.KBlockUtils;

import com.mojang.serialization.Codec;

import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

public record ParsedProtoTag(String prefix, String key, String value) {
	public static ParsedProtoTag of(String s) {
		String prefix;
		if (s.startsWith("*") || s.startsWith("@")) {
			prefix = s.substring(0, 1);
			s = s.substring(1);
		} else {
			prefix = "";
		}
		int i = s.indexOf(':');
		String key = i == -1 ? s : s.substring(0, i);
		String value = i == -1 ? "" : s.substring(i + 1);
		return new ParsedProtoTag(prefix, key, value);
	}

	public static final Codec<ParsedProtoTag> CODEC = ExtraCodecs.NON_EMPTY_STRING.xmap(ParsedProtoTag::of, ParsedProtoTag::toString);

	public String prefixedKey() {
		return this.prefix + this.key;
	}

	public ParsedProtoTag resolve(BlockState blockState) {
		return resolve(blockState, Rotation.NONE);
	}

	public ParsedProtoTag resolve(BlockState blockState, Rotation rotation) {
		if (isResolved()) {
			return this;
		} else {
			String newValue;
			if (value.isEmpty()) {
				newValue = KBlockUtils.getValueString(blockState, key);
			} else {
				Direction direction = Direction.valueOf(value.toUpperCase(Locale.ENGLISH));
				newValue = rotation.rotate(direction).getSerializedName();
			}
			return new ParsedProtoTag("", key, newValue);
		}
	}

	public boolean isResolved() {
		return !prefix.equals("@");
	}

	@Override
	public String toString() {
		return this.prefix + this.key + (this.value.isEmpty() ? "" : ":" + this.value);
	}
}

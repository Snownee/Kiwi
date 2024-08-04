package snownee.kiwi.customization.block;

import java.util.Collection;
import java.util.Optional;

import snownee.kiwi.util.NotNullByDefault;

import com.google.common.collect.ImmutableSortedSet;

import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

@NotNullByDefault
public class StringProperty extends Property<String> {
	private final ImmutableSortedSet<String> values;

	public StringProperty(String pName, Collection<String> values) {
		super(pName, String.class);
		this.values = ImmutableSortedSet.copyOf(values.stream().map(String::intern).toList());
	}

	public static StringProperty convert(EnumProperty<?> property) {
		return KBlockUtils.internProperty(new StringProperty(
				property.getName(),
				property.getPossibleValues().stream().map(v -> KBlockUtils.getNameByValue(property, v)).toList()));
	}

	@Override
	public Collection<String> getPossibleValues() {
		return values;
	}

	@Override
	public String getName(String value) {
		return value;
	}

	@Override
	public Optional<String> getValue(String key) {
		return values.contains(key) ? Optional.of(key) : Optional.empty();
	}

	@Override
	public boolean equals(Object pOther) {
		if (this == pOther) {
			return true;
		} else if (pOther instanceof StringProperty stringProperty) {
			return getName().equals(stringProperty.getName()) && values.equals(stringProperty.values);
		} else {
			return false;
		}
	}

	@Override
	public int generateHashCode() {
		return 31 * getName().hashCode() + values.hashCode();
	}
}

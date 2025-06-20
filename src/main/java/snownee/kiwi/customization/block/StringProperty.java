package snownee.kiwi.customization.block;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class StringProperty extends Property<String> {
	private final List<String> values;

	public StringProperty(String pName, Collection<String> values) {
		super(pName, String.class);
		this.values = values.stream().map(String::intern).toList();
	}

	public static StringProperty convert(EnumProperty<?> property) {
		return KBlockUtils.internProperty(new StringProperty(
				property.getName(),
				property.getPossibleValues().stream().map(v -> KBlockUtils.getNameByValue(property, v)).toList()));
	}

	@Override
	public List<String> getPossibleValues() {
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
	public int getInternalIndex(String key) {
		return values.indexOf(key);
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

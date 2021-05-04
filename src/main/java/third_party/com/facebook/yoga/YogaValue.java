package third_party.com.facebook.yoga;

/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * Copyright (c) 2018-present, Marius Klimantavičius
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

public class YogaValue {
	public static final YogaValue UNDEFINED = new YogaValue(Float.NaN, YogaUnit.Undefined);
	public static final YogaValue ZERO = new YogaValue(0, YogaUnit.Point);
	public static final YogaValue AUTO = new YogaValue(Float.NaN, YogaUnit.Auto);

	public final float Value;
	public final YogaUnit Unit;

	public YogaValue(float Value, YogaUnit Unit) {
		this.Value = Value;
		this.Unit = Unit;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof YogaValue) {
			final YogaValue otherValue = (YogaValue) other;
			if (Unit == otherValue.Unit) {
				return Unit == YogaUnit.Undefined || Unit == YogaUnit.Auto || Float.compare(Value, otherValue.Value) == 0;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Float.floatToIntBits(Value) + Unit.ordinal();
	}

	@Override
	public String toString() {
		switch (Unit) {
		case Undefined:
			return "undefined";
		case Point:
			return Float.toString(Value);
		case Percent:
			return Value + "%";
		case Auto:
			return "auto";
		default:
			throw new IllegalStateException();
		}
	}

	public static YogaValue Parse(String s) {
		if (s == null) {
			return null;
		}

		if ("undefined".equals(s)) {
			return UNDEFINED;
		}

		if ("auto".equals(s)) {
			return AUTO;
		}

		if (s.endsWith("%")) {
			return new YogaValue(Float.parseFloat(s.substring(0, s.length() - 1)), YogaUnit.Percent);
		}

		return new YogaValue(Float.parseFloat(s), YogaUnit.Point);
	}

	public float Resolve(float ownerSize) {
		switch (Unit) {
		default:
			return Float.NaN;
		case Point:
			return Value;
		case Percent:
			return Value * ownerSize * 0.01f;
		}
	}

	public static YogaValue Percent(float value) {
		return new YogaValue(value, YogaUnit.Percent);
	}

	public static YogaValue Pt(float value) {
		return new YogaValue(value, YogaUnit.Point);
	}

}

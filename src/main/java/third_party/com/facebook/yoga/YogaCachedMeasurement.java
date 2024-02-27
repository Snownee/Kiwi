package third_party.com.facebook.yoga;

import com.google.common.base.Objects;

/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * Copyright (c) 2018-present, Marius Klimantavičius
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

public class YogaCachedMeasurement {
	public float AvailableWidth;
	public float AvailableHeight;
	public YogaMeasureMode WidthMeasureMode;
	public YogaMeasureMode HeightMeasureMode;

	public float ComputedWidth = -1;
	public float ComputedHeight = -1;

	public void CopyFrom(YogaCachedMeasurement other) {
		AvailableWidth = other.AvailableWidth;
		AvailableHeight = other.AvailableHeight;
		WidthMeasureMode = other.WidthMeasureMode;
		HeightMeasureMode = other.HeightMeasureMode;
		ComputedWidth = other.ComputedWidth;
		ComputedHeight = other.ComputedHeight;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof YogaCachedMeasurement)) {
			return false;
		}
		YogaCachedMeasurement measurement = (YogaCachedMeasurement) o;
		boolean isEqual = WidthMeasureMode == measurement.WidthMeasureMode && HeightMeasureMode == measurement.HeightMeasureMode;

		if (!Float.isNaN(AvailableWidth) || !Float.isNaN(measurement.AvailableWidth)) {
			isEqual = isEqual && AvailableWidth == measurement.AvailableWidth;
		}
		if (!Float.isNaN(AvailableHeight) || !Float.isNaN(measurement.AvailableHeight)) {
			isEqual = isEqual && AvailableHeight == measurement.AvailableHeight;
		}
		if (!Float.isNaN(ComputedWidth) || !Float.isNaN(measurement.ComputedWidth)) {
			isEqual = isEqual && ComputedWidth == measurement.ComputedWidth;
		}
		if (!Float.isNaN(ComputedHeight) || !Float.isNaN(measurement.ComputedHeight)) {
			isEqual = isEqual && ComputedHeight == measurement.ComputedHeight;
		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(AvailableWidth, AvailableHeight, WidthMeasureMode, HeightMeasureMode, ComputedWidth, ComputedHeight);
	}

	public void Clear() {
		AvailableWidth = 0;
		AvailableHeight = 0;
		WidthMeasureMode = null;
		HeightMeasureMode = null;
		ComputedWidth = -1;
		ComputedHeight = -1;
	}

}

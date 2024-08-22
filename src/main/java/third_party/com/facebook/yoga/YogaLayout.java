package third_party.com.facebook.yoga;

import java.util.Arrays;

import com.google.common.base.Objects;

/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * Copyright (c) 2018-present, Marius Klimantavičius
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

public final class YogaLayout {
	public static final int MaxCachedResultCount = 8; // 16;

	public final float[] Position = new float[4]; // 4
	public final float[] Dimensions = new float[]{Float.NaN, Float.NaN}; // 2
	public final float[] Margin = new float[6]; // 6
	public final float[] Border = new float[6]; // 6
	public final float[] Padding = new float[6]; // 6
	public YogaDirection Direction;

	public int ComputedFlexBasisGeneration;
	public float ComputedFlexBasis;
	public boolean HadOverflow;

	// Instead of recomputing the entire layout every single time, we
	// cache some information to break early when nothing changed
	public int GenerationCount;
	public YogaDirection LastOwnerDirection;

	public int NextCachedMeasurementsIndex;
	public YogaCachedMeasurement[] CachedMeasurements; // MaxCachedResultCount
	public final float[] MeasuredDimensions = new float[]{Float.NaN, Float.NaN}; // 2

	public YogaCachedMeasurement CachedLayout;
	public boolean DidUseLegacyFlag;
	public boolean DoesLegacyStretchFlagAffectsLayout;

	public YogaLayout() {
		YogaCachedMeasurement[] cached = new YogaCachedMeasurement[MaxCachedResultCount];
		for (int i = 0; i < cached.length; i++)
			cached[i] = new YogaCachedMeasurement();

		Direction = YogaDirection.Inherit;
		ComputedFlexBasisGeneration = 0;
		ComputedFlexBasis = Float.NaN;
		HadOverflow = false;
		GenerationCount = 0;
		NextCachedMeasurementsIndex = 0;
		CachedMeasurements = cached;
		CachedLayout = new YogaCachedMeasurement();
		DidUseLegacyFlag = false;
		DoesLegacyStretchFlagAffectsLayout = false;
	}

	public static boolean Equal(YogaLayout self, YogaLayout layout) {
		if (self == layout) {
			return true;
		}

		if (self == null || layout == null) {
			return false;
		}

		boolean isEqual = Arrays.equals(self.Position, layout.Position) && Arrays.equals(self.Dimensions, layout.Dimensions) &&
				Arrays.equals(self.Margin, layout.Margin) && Arrays.equals(self.Border, layout.Border) && Arrays.equals(
				self.Padding,
				layout.Padding) && self.Direction == layout.Direction && self.HadOverflow == layout.HadOverflow &&
				self.LastOwnerDirection == layout.LastOwnerDirection &&
				self.NextCachedMeasurementsIndex == layout.NextCachedMeasurementsIndex && self.CachedLayout == layout.CachedLayout;

		for (int i = 0; i < MaxCachedResultCount && isEqual; ++i)
			isEqual = isEqual && self.CachedMeasurements[i] == layout.CachedMeasurements[i];

		isEqual = isEqual && (self.ComputedFlexBasis == layout.ComputedFlexBasis);
		isEqual = isEqual && (self.MeasuredDimensions[0] == layout.MeasuredDimensions[0]);
		return isEqual && (self.MeasuredDimensions[1] == layout.MeasuredDimensions[1]);
	}

	public void CopyFrom(YogaLayout other) {
		ArrayUtil.copy(other.Position, Position);
		ArrayUtil.copy(other.Dimensions, Dimensions);
		ArrayUtil.copy(other.Margin, Margin);
		ArrayUtil.copy(other.Border, Border);
		ArrayUtil.copy(other.Padding, Padding);
		Direction = other.Direction;
		ComputedFlexBasisGeneration = other.ComputedFlexBasisGeneration;
		ComputedFlexBasis = other.ComputedFlexBasis;
		HadOverflow = other.HadOverflow;
		GenerationCount = other.GenerationCount;
		LastOwnerDirection = other.LastOwnerDirection;
		NextCachedMeasurementsIndex = other.NextCachedMeasurementsIndex;
		CachedMeasurements = Arrays.copyOf(other.CachedMeasurements, CachedMeasurements.length);
		ArrayUtil.copy(other.MeasuredDimensions, MeasuredDimensions);
		CachedLayout.CopyFrom(other.CachedLayout);
		DidUseLegacyFlag = other.DidUseLegacyFlag;
		DoesLegacyStretchFlagAffectsLayout = other.DoesLegacyStretchFlagAffectsLayout;
	}

	public void Clear() {
		Arrays.fill(Position, 0);
		Arrays.fill(Dimensions, 0);
		Arrays.fill(Margin, 0);
		Arrays.fill(Border, 0);
		Arrays.fill(Padding, 0);
		Direction = YogaDirection.Inherit;
		ComputedFlexBasisGeneration = 0;
		ComputedFlexBasis = 0F;
		HadOverflow = false;
		GenerationCount = 0;
		LastOwnerDirection = YogaDirection.Inherit;
		NextCachedMeasurementsIndex = 0;
		Arrays.fill(MeasuredDimensions, 0);
		CachedLayout.Clear();
		DidUseLegacyFlag = false;
		DoesLegacyStretchFlagAffectsLayout = false;

		for (YogaCachedMeasurement item : CachedMeasurements)
			item.Clear();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(
				Position,
				Dimensions,
				Margin,
				Border,
				Padding,
				Direction,
				ComputedFlexBasisGeneration,
				ComputedFlexBasis,
				HadOverflow,
				GenerationCount,
				LastOwnerDirection,
				NextCachedMeasurementsIndex,
				CachedMeasurements,
				MeasuredDimensions,
				CachedLayout,
				DidUseLegacyFlag,
				DoesLegacyStretchFlagAffectsLayout);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != YogaLayout.class) {
			return false;
		}
		return Equal(this, (YogaLayout) obj);
	}
}

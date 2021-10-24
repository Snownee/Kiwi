package third_party.com.facebook.yoga;

import java.util.Arrays;

import com.google.common.base.Objects;

/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * Copyright (c) 2018-present, Marius Klimantavičius
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

public final class YogaStyle {
	private static final YogaValue[] DefaultEdgeValuesUnit = new YogaValue[] { YogaValue.UNDEFINED, YogaValue.UNDEFINED, YogaValue.UNDEFINED, YogaValue.UNDEFINED, YogaValue.UNDEFINED, YogaValue.UNDEFINED, YogaValue.UNDEFINED, YogaValue.UNDEFINED, YogaValue.UNDEFINED };

	private static final YogaValue[] DefaultDimensionValuesAutoUnit = new YogaValue[] { YogaValue.AUTO, YogaValue.AUTO };
	private static final YogaValue[] DefaultDimensionValuesUnit = new YogaValue[] { YogaValue.UNDEFINED, YogaValue.UNDEFINED };

	public YogaDirection Direction = YogaDirection.Inherit;
	public YogaFlexDirection FlexDirection = YogaFlexDirection.Column;
	public YogaJustify JustifyContent = YogaJustify.FlexStart;
	public YogaAlign AlignContent = YogaAlign.FlexStart;
	public YogaAlign AlignItems = YogaAlign.Stretch;
	public YogaAlign AlignSelf = YogaAlign.Auto;
	public YogaPositionType PositionType = YogaPositionType.Relative;
	public YogaWrap FlexWrap = YogaWrap.NoWrap;
	public YogaOverflow Overflow = YogaOverflow.Visible;
	public YogaDisplay Display = YogaDisplay.Flex;
	public float Flex = Float.NaN;
	public float FlexGrow = Float.NaN;
	public float FlexShrink = Float.NaN;
	public YogaValue FlexBasis = YogaValue.AUTO;
	public YogaValue[] Margin; // YGEdgeCount
	public YogaValue[] Position; // YGEdgeCount
	public YogaValue[] Padding; // YGEdgeCount
	public YogaValue[] Border; // YGEdgeCount
	public YogaValue[] Dimensions; // 2
	public YogaValue[] MinDimensions; // 2
	public YogaValue[] MaxDimensions; // 2

	public float AspectRatio = Float.NaN;

	public YogaStyle() {
		Margin = Arrays.copyOf(DefaultEdgeValuesUnit, DefaultEdgeValuesUnit.length);
		Position = Arrays.copyOf(DefaultEdgeValuesUnit, DefaultEdgeValuesUnit.length);
		Padding = Arrays.copyOf(DefaultEdgeValuesUnit, DefaultEdgeValuesUnit.length);
		Border = Arrays.copyOf(DefaultEdgeValuesUnit, DefaultEdgeValuesUnit.length);
		Dimensions = Arrays.copyOf(DefaultDimensionValuesAutoUnit, DefaultDimensionValuesAutoUnit.length);
		MinDimensions = Arrays.copyOf(DefaultDimensionValuesUnit, DefaultDimensionValuesUnit.length);
		MaxDimensions = Arrays.copyOf(DefaultDimensionValuesUnit, DefaultDimensionValuesUnit.length);
	}

	public void CopyFrom(YogaStyle other) {
		Direction = other.Direction;
		FlexDirection = other.FlexDirection;
		JustifyContent = other.JustifyContent;
		AlignContent = other.AlignContent;
		AlignItems = other.AlignItems;
		AlignSelf = other.AlignSelf;
		PositionType = other.PositionType;
		FlexWrap = other.FlexWrap;
		Overflow = other.Overflow;
		Display = other.Display;
		Flex = other.Flex;
		FlexGrow = other.FlexGrow;
		FlexShrink = other.FlexShrink;
		FlexBasis = other.FlexBasis;
		ArrayUtil.copy(other.Margin, Margin);
		ArrayUtil.copy(other.Position, Position);
		ArrayUtil.copy(other.Padding, Padding);
		ArrayUtil.copy(other.Border, Border);
		ArrayUtil.copy(other.Dimensions, Dimensions);
		ArrayUtil.copy(other.MinDimensions, MinDimensions);
		ArrayUtil.copy(other.MaxDimensions, MaxDimensions);
		AspectRatio = other.AspectRatio;
	}

	// Yoga specific properties, not compatible with flexbox specification
	public static boolean Equal(YogaStyle self, YogaStyle style) {
		if (self == style)
			return true;

		if (self == null || style == null)
			return false;

		/* off */
        boolean areNonFloatValuesEqual = self.Direction == style.Direction
                && self.FlexDirection == style.FlexDirection
                && self.JustifyContent == style.JustifyContent
                && self.AlignContent == style.AlignContent
                && self.AlignItems == style.AlignItems
                && self.AlignSelf == style.AlignSelf
                && self.PositionType == style.PositionType
                && self.FlexWrap == style.FlexWrap
                && self.Overflow == style.Overflow
                && self.Display == style.Display
                && java.util.Objects.equals(self.FlexBasis, style.FlexBasis)
                && Arrays.equals(self.Margin, style.Margin)
                && Arrays.equals(self.Position, style.Position)
                && Arrays.equals(self.Padding, style.Padding)
                && Arrays.equals(self.Border, style.Border)
                && Arrays.equals(self.Dimensions, style.Dimensions)
                && Arrays.equals(self.MinDimensions, style.MinDimensions)
                && Arrays.equals(self.MaxDimensions, style.MaxDimensions);
        /* on */

		areNonFloatValuesEqual = areNonFloatValuesEqual && self.Flex == style.Flex;

		areNonFloatValuesEqual = areNonFloatValuesEqual && self.FlexGrow == style.FlexGrow;
		areNonFloatValuesEqual = areNonFloatValuesEqual && self.FlexShrink == style.FlexShrink;
		return areNonFloatValuesEqual && self.AspectRatio == style.AspectRatio;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != YogaStyle.class) {
			return false;
		}
		return Equal(this, (YogaStyle) obj);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(Direction, FlexDirection, JustifyContent, AlignContent, AlignItems, AlignSelf, PositionType, FlexWrap, Overflow, Display, Flex, FlexGrow, FlexShrink, FlexBasis, Margin, Position, Padding, Border, Dimensions, MinDimensions, MaxDimensions, AspectRatio);
	}
}

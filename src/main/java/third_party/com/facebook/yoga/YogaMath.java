package third_party.com.facebook.yoga;

/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * Copyright (c) 2018-present, Marius Klimantavičius
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

public class YogaMath {

    public static float Max(float a, float b) {
        if (!Float.isNaN(a) && !Float.isNaN(b))
            return Math.max(a, b);

        return Float.isNaN(a) ? b : a;
    }

    public static float Min(float a, float b) {
        if (!Float.isNaN(a) && !Float.isNaN(b))
            return Math.min(a, b);

        return Float.isNaN(a) ? b : a;
    }

    public static float RoundValueToPixelGrid(float value, float pointScaleFactor, boolean forceCeil, boolean forceFloor) {
        float scaledValue = value * pointScaleFactor;

        // We want to calculate `fractial` such that `floor(scaledValue) = scaledValue
        // - fractial`.
        float fractial = scaledValue % 1.0f;
        if (fractial < 0) {
            // This branch is for handling negative numbers for `value`.
            //
            // Regarding `floor` and `ceil`. Note that for a number x, `floor(x) <= x <=
            // ceil(x)` even for negative numbers. Here are a couple of examples:
            //   - x =  2.2: floor( 2.2) =  2, ceil( 2.2) =  3
            //   - x = -2.2: floor(-2.2) = -3, ceil(-2.2) = -2
            //
            // Regarding `fmodf`. For fractional negative numbers, `fmodf` returns a
            // negative number. For example, `fmodf(-2.2) = -0.2`. However, we want
            // `fractial` to be the number such that subtracting it from `value` will
            // give us `floor(value)`. In the case of negative numbers, adding 1 to
            // `fmodf(value)` gives us this. Let's continue the example from above:
            //   - fractial = fmodf(-2.2) = -0.2
            //   - Add 1 to the fraction: fractial2 = fractial + 1 = -0.2 + 1 = 0.8
            //   - Finding the `floor`: -2.2 - fractial2 = -2.2 - 0.8 = -3
            ++fractial;
        }

        if (FloatsEqual(fractial, 0)) {
            // First we check if the value is already rounded
            scaledValue = scaledValue - fractial;
        } else if (FloatsEqual(fractial, 1.0f)) {
            scaledValue = scaledValue - fractial + 1.0f;
        } else if (forceCeil) {
            // Next we check if we need to use forced rounding
            scaledValue = scaledValue - fractial + 1.0f;
        } else if (forceFloor) {
            scaledValue = scaledValue - fractial;
        } else {
            // Finally we just round the value
            scaledValue = scaledValue - fractial + (!Float.isNaN(fractial) && (fractial > 0.5f || FloatsEqual(fractial, 0.5f)) ? 1.0f : 0.0f);
        }
        return (Float.isNaN(scaledValue) || Float.isNaN(pointScaleFactor)) ? Float.NaN : scaledValue / pointScaleFactor;
    }

    public static boolean FloatsEqual(float a, float b) {
        if (Float.isNaN(a))
            return Float.isNaN(b);

        if (Float.isNaN(b))
            return false;

        return Math.abs(a - b) < 0.0001f;
    }

}

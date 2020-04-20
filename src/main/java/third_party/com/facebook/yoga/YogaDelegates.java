package third_party.com.facebook.yoga;

/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * Copyright (c) 2018-present, Marius Klimantavičius
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

public final class YogaDelegates {
    @FunctionalInterface
    public interface YogaMeasure {
        YogaSize apply(YogaNode node, float width, YogaMeasureMode widthMode, float height, YogaMeasureMode heightMode);
    }

    @FunctionalInterface
    public interface YogaBaseline {
        float apply(YogaNode node, float width, float height);
    }

    @FunctionalInterface
    public interface YogaNodeCloned {
        void apply(YogaNode oldNode, YogaNode newNode, YogaNode owner, int childIndex);
    }
}

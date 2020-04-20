package third_party.com.facebook.yoga;

/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * Copyright (c) 2018-present, Marius Klimantavičius
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

public enum YogaFlexDirection {
    Column,
    ColumnReverse,
    Row,
    RowReverse;

    public boolean IsRow() {
        return this == Row || this == RowReverse;
    }

    public boolean IsColumn() {
        return this == Column || this == ColumnReverse;
    }

    public YogaFlexDirection ResolveFlexDirection(YogaDirection direction) {
        if (direction == YogaDirection.RightToLeft) {
            if (this == Row)
                return RowReverse;
            else if (this == RowReverse)
                return Row;
        }
        return this;
    }

    public YogaFlexDirection FlexDirectionCross(YogaDirection direction) {
        return this.IsColumn() ? Row.ResolveFlexDirection(direction) : Column;
    }
}

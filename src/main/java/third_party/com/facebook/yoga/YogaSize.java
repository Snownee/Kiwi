package third_party.com.facebook.yoga;

/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * Copyright (c) 2018-present, Marius Klimantavičius
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

public class YogaSize {
	public float Width;
	public float Height;

	public static YogaSize From(float width, float height) {
		YogaSize size = new YogaSize();
		size.Width = width;
		size.Height = height;
		return size;
	}
}

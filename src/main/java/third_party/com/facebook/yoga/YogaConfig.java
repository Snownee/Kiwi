package third_party.com.facebook.yoga;

import java.util.Arrays;

import third_party.com.facebook.yoga.YogaDelegates.YogaNodeCloned;

/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * Copyright (c) 2018-present, Marius Klimantavičius
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

public class YogaConfig {

	public boolean[] ExperimentalFeatures = new boolean[] { false };
	public boolean UseWebDefaults;
	public boolean UseLegacyStretchBehaviour;
	public boolean ShouldDiffLayoutWithoutLegacyStretchBehaviour;
	public float PointScaleFactor = 1.0F;
	public YogaNodeCloned OnNodeCloned;

	public YogaConfig() {
	}

	public YogaConfig(YogaConfig oldConfig) {
		ExperimentalFeatures = new boolean[oldConfig.ExperimentalFeatures.length];

		ExperimentalFeatures = Arrays.copyOf(oldConfig.ExperimentalFeatures, ExperimentalFeatures.length);
		UseWebDefaults = oldConfig.UseWebDefaults;
		UseLegacyStretchBehaviour = oldConfig.UseLegacyStretchBehaviour;
		ShouldDiffLayoutWithoutLegacyStretchBehaviour = oldConfig.ShouldDiffLayoutWithoutLegacyStretchBehaviour;
		PointScaleFactor = oldConfig.PointScaleFactor;
		OnNodeCloned = oldConfig.OnNodeCloned;
	}

	public boolean IsExperimentalFeatureEnabled(YogaExperimentalFeature feature) {
		return ExperimentalFeatures[feature.ordinal()];
	}

	public YogaConfig DeepClone() {
		return new YogaConfig(this);
	}
}

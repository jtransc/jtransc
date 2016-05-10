/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.ast

import com.jtransc.ast.feature.GotosFeature
import com.jtransc.ast.feature.OptimizeFeature
import com.jtransc.ast.feature.SimdFeature
import com.jtransc.ast.feature.SwitchesFeature
import com.jtransc.ast.transform.CombineNewInitTransform
import com.jtransc.ast.transform.RemoveTransitiveLocalsTransform

class AstFeatures {
	internal val AVAILABLE_FEATUES = arrayListOf<AstFeature>(
		OptimizeFeature,
		GotosFeature,
		SwitchesFeature,
		SimdFeature
	)

	internal val TRANSFORMS = arrayListOf<AstTransform>(
		CombineNewInitTransform,
		RemoveTransitiveLocalsTransform
	)

	fun registerLast(feature: AstFeature): AstFeatures {
		if (feature !in AVAILABLE_FEATUES) AVAILABLE_FEATUES.add(feature)
		return this
	}

	fun registerBefore(feature: AstFeature, pivot: AstFeature): AstFeatures {
		if (feature !in AVAILABLE_FEATUES) {
			AVAILABLE_FEATUES.add(AVAILABLE_FEATUES.indexOf(pivot) - 1, feature)
		}
		return this
	}

	fun apply(body: AstBody, supportedFeatures: Set<AstFeature>, settings: AstBuildSettings): AstBody {
		var out = body
		for (transform in TRANSFORMS) {
			out = transform(out)
		}
		for (feature in AVAILABLE_FEATUES) {
			val included = (feature in supportedFeatures)
			if (included) {
				out = feature.add(out, settings)
			} else {
				out = feature.remove(out, settings)
			}
		}
		return out
	}
}

open class AstFeature {
	open fun remove(body: AstBody, settings: AstBuildSettings): AstBody = body
	open fun add(body: AstBody, settings: AstBuildSettings): AstBody = body
}

open class AstTransform {
	operator open fun invoke(body: AstBody): AstBody = body
	operator open fun invoke(body: AstProgram): AstProgram = body
}

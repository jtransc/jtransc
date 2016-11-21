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

import com.jtransc.ast.transform.CombineNewInitTransform
import com.jtransc.ast.transform.RemoveTransitiveLocalsTransform
import com.jtransc.injector.Singleton
import java.util.*

@Singleton
class AstMethodFeatures {
	internal val AVAILABLE_FEATUES = ServiceLoader.load(AstMethodFeature::class.java).toMutableList()

	internal val TRANSFORMS = arrayListOf<AstTransform>(
		CombineNewInitTransform,
		RemoveTransitiveLocalsTransform
	)

	fun registerLast(feature: AstMethodFeature): AstMethodFeatures {
		if (feature !in AVAILABLE_FEATUES) AVAILABLE_FEATUES.add(feature)
		return this
	}

	fun registerBefore(feature: AstMethodFeature, pivot: AstMethodFeature): AstMethodFeatures {
		if (feature !in AVAILABLE_FEATUES) {
			AVAILABLE_FEATUES.add(AVAILABLE_FEATUES.indexOf(pivot) - 1, feature)
		}
		return this
	}

	fun apply(method: AstMethod, body: AstBody, supportedFeatures: Set<Class<out AstMethodFeature>>, settings: AstBuildSettings, types: AstTypes): AstBody {
		var out = body
		for (transform in TRANSFORMS) {
			out = transform(out)
		}
		for (feature in AVAILABLE_FEATUES) {
			if (feature.javaClass in supportedFeatures) {
				out = feature.add(method, out, settings, types)
			} else {
				out = feature.remove(method, out, settings, types)
			}
		}
		return out
	}
}

open class AstMethodFeature {
	//open val dependsOn = setOf<AstMethodFeature>()
	open val priority: Int = 0
	open fun remove(method: AstMethod, body: AstBody, settings: AstBuildSettings, types: AstTypes): AstBody = body
	open fun add(method: AstMethod, body: AstBody, settings: AstBuildSettings, types: AstTypes): AstBody = body
}

open class AstProgramFeature {
	open val priority: Int = 0
	//open val dependsOn = setOf<AstProgramFeature>()
	open fun onMissing(program: AstProgram, settings: AstBuildSettings, types: AstTypes): Unit = Unit
	open fun onSupported(program: AstProgram, settings: AstBuildSettings, types: AstTypes): Unit = Unit
}

open class AstTransform {
	operator open fun invoke(body: AstBody): AstBody = body
	operator open fun invoke(body: AstProgram): AstProgram = body
}

/*
* Copyright 2014 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package asset.pipeline.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.FileSystemAssetResolver

/**
 * This is the Gradle Plugin implementation of asset-pipeline-core. It provides a set of tasks useful for working with your assets directly
 *
 * task: asset-compile Compiles your assets into your build directory
 * task: asset-clean Cleans the build/assets directory
 *
 * @author David Estes
 * @author Graeme Rocher
*/
class AssetPipelinePlugin implements Plugin<Project> {

	void apply(Project project) {
		AssetPipelineExtension assetPipeline = project.extensions.create('assets', AssetPipelineExtension)


		def assetPrecompileTask = project.task('asset-precompile')
		assetPrecompileTask << {
            def assetsPath = "${project.projectDir}/$assetPipeline.assetsPath"
            def resolver = new FileSystemAssetResolver('application', assetsPath)
            AssetPipelineConfigHolder.registerResolver(resolver)

			def assetCompiler = new AssetCompiler(assetPipeline.toMap(),new GradleEventListener())
			assetCompiler.excludeRules.default = assetPipeline.excludes
			assetCompiler.includeRules.default = assetPipeline.includes
			assetCompiler.compile()
		}

		def assembleTask = project.tasks.findByName('assemble')
		if(assembleTask) {
			assembleTask.dependsOn(assetPrecompileTask)
		}

//		project.task('asset-clean') << {
//			//TODO: REMOVE target compileDir
//		}

		// project.task('asset-watch') << {
		// 	//TODO: Implement live watcher to auto recompile assets as they change
		// }
	}
}

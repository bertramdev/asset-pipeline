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
import org.gradle.api.tasks.Delete

/**
 * This is the Gradle Plugin implementation of asset-pipeline-core. It provides a set of tasks useful for working with your assets directly
 *
 * task: assetCompile Compiles your assets into your build directory
 * task: assetClean Cleans the build/assets directory
 *
 * @author David Estes
 * @author Graeme Rocher
*/
class AssetPipelinePlugin implements Plugin<Project> {

	void apply(Project project) {
        def defaultConfiguration = project.extensions.create('assets', AssetPipelineExtension)

        if(project.extensions.findByName('grails')) {
            defaultConfiguration.assetsPath = 'grails-app/assets'
            defaultConfiguration.compileDir = 'build/assetCompile/assets'
        }

		project.tasks.create('assetCompile', AssetCompile)

        def assetPrecompileTask = project.tasks.getByName('assetCompile')
        def assetCleanTask = project.tasks.create('assetClean', Delete)

        project.afterEvaluate {
            def assetPipeline = project.extensions.getByType(AssetPipelineExtension)
            assetCleanTask.configure {
                delete project.file(assetPipeline.compileDir)
            }
            assetPrecompileTask.configure {
                destinationDir = project.file(assetPipeline.compileDir)
                assetsDir = project.file(assetPipeline.assetsPath)
                minifyJs = assetPipeline.minifyJs
                minifyCss = assetPipeline.minifyCss
                minifyOptions = assetPipeline.minifyOptions
                includes = assetPipeline.includes
                excludes = assetPipeline.excludes
                excludesGzip = assetPipeline.excludesGzip
				config = assetPipeline.config
            }
        }
		def assembleTask = project.tasks.findByName('assemble')
		if(assembleTask) {
			assembleTask.dependsOn( assetPrecompileTask )
		}

        def cleanTask = project.tasks.findByName('clean')
        if(cleanTask) {
            cleanTask.dependsOn( assetCleanTask )
        }


		// project.task('asset-watch') << {
		// 	//TODO: Implement live watcher to auto recompile assets as they change
		// }
	}
}

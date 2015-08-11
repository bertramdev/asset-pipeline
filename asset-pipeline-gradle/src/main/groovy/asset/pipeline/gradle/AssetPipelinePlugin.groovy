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
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Jar
/**
 * This is the Gradle Plugin implementation of asset-pipeline-core. It provides a set of tasks useful for working with your assets directly
 *
 * task: assetCompile Compiles your assets into your build directory
 * task: assetClean Cleans the build/assets directory
 *
 * @author David Estes
 * @author Graeme Rocher
 * @author Craig Burke 
*/
class AssetPipelinePlugin implements Plugin<Project> {

    static final String ASSET_CONFIGURATION_NAME = 'assets'
    
	void apply(Project project) {
        createGradleConfiguration(project)
        
        def defaultConfiguration = project.extensions.create('assets', AssetPipelineExtension)

        if(project.extensions.findByName('grails')) {
            defaultConfiguration.assetsPath = 'grails-app/assets'
        } else {
          defaultConfiguration.assetsPath = "${project.projectDir}/src/assets"
        }
        defaultConfiguration.compileDir = "${project.buildDir}/assets"

		project.tasks.create('assetCompile', AssetCompile)


        def assetPrecompileTask = project.tasks.getByName('assetCompile')
        // assetPrecompileTask.dependsOn('classes')
        def assetCleanTask = project.tasks.create('assetClean', Delete)

        project.afterEvaluate {
            def assetPipeline = project.extensions.getByType(AssetPipelineExtension)
            assetCleanTask.configure {
                delete project.file(assetPipeline.compileDir)
            }
            def configDestinationDir = project.file(assetPipeline.compileDir)
            
            
            assetPrecompileTask.configure {
                destinationDir = configDestinationDir
                assetsDir = project.file(assetPipeline.assetsPath)
                minifyJs = assetPipeline.minifyJs
                minifyCss = assetPipeline.minifyCss
                minifyOptions = assetPipeline.minifyOptions
                includes = assetPipeline.includes
                excludes = assetPipeline.excludes
                excludesGzip = assetPipeline.excludesGzip
				configOptions = assetPipeline.configOptions
                skipNonDigests = assetPipeline.skipNonDigests
                enableDigests = assetPipeline.enableDigests
                enableSourceMaps = assetPipeline.enableSourceMaps
                resolvers = assetPipeline.resolvers
            }

            project.tasks.withType(Jar) { Jar bundleTask ->
                bundleTask.dependsOn assetPrecompileTask
                bundleTask.from assetPipeline.compileDir, {
                    into "assets"
                }
            }

        }

		// def assembleTask = project.tasks.findByName('assemble')
		// if(assembleTask) {
		// 	assembleTask.dependsOn( assetPrecompileTask )
		// }

  //       def cleanTask = project.tasks.findByName('clean')
  //       if(cleanTask) {
  //           cleanTask.dependsOn( assetCleanTask )
  //       }

  //       def jarTask = project.tasks.findByName('jar')
  //       if(jarTask) {
  //           jarTask.dependsOn(assetPrecompileTask)
  //       }

  //       def shadowJarTask = project.tasks.findByName('shadowJar')
  //       if(shadowJarTask) {
  //           shadowJarTask.dependsOn(assetPrecompileTask)
  //       }

  //       def warTask = project.tasks.findByName('war')
  //       if(warTask) {
  //           warTask.dependsOn(assetPrecompileTask)
  //       }

  //       def installAppTask = project.tasks.findByName('installApp')
  //       if(installAppTask) {
  //           installAppTask.dependsOn(assetPrecompileTask)
  //       }

  //       def installDistTask = project.tasks.findByName('installDist')
  //       if(installDistTask) {
  //           installDistTask.dependsOn(assetPrecompileTask)
  //       }

        
        // def resourcesTask = project.tasks.findByName('classes')

               
		// project.task('asset-watch') << {
		// 	//TODO: Implement live watcher to auto recompile assets as they change
		// }
	}
    
    private void createGradleConfiguration(Project project) {
        Configuration configuration = project.configurations.create(ASSET_CONFIGURATION_NAME)
        project.plugins.withType(JavaPlugin) {
            Configuration runtimeConfiguration = project.configurations.getByName(JavaPlugin.RUNTIME_CONFIGURATION_NAME)
            runtimeConfiguration.extendsFrom configuration
        }
    }
    
}

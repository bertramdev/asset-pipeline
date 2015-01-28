package asset.pipeline.gradle

import asset.pipeline.AssetCompiler
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.FileSystemAssetResolver
import asset.pipeline.fs.JarAssetResolver
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.file.FileCollection

/*
 * Copyright 2014 original authors
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

/**
 * A Gradle task for compiling assets
 *
 * @author Graeme Rocher
 */
@CompileStatic
class AssetCompile extends DefaultTask {

    @Delegate AssetPipelineExtension pipelineExtension = new AssetPipelineExtension()
    private FileCollection classpath;

    @OutputDirectory
    File getDestinationDir() {
        pipelineExtension.compileDir ? new File(pipelineExtension.compileDir) : null
    }
    void setDestinationDir(File dir) {
        pipelineExtension.compileDir = dir.absolutePath
    }

    @Input
    File getAssetsDir() {
        def path = pipelineExtension.assetsPath
        return path ? new File(path) : null
    }

    void setAssetsDir(File assetsDir) {
        pipelineExtension.assetsPath = assetsDir.absolutePath
    }

    @Input
    @Optional
    boolean getMinifyJs() {
        pipelineExtension.minifyJs
    }

    void setMinifyJs(boolean minifyJs) {
        pipelineExtension.minifyJs = minifyJs
    }

    @Input
    @Optional
    boolean getMinifyCss() {
        pipelineExtension.minifyCss
    }

    @Input
    @Optional
    Map getConfigOptions() {
        pipelineExtension.configOptions
    }

    void setConfig(Map configOptions) {
        pipelineExtension.configOptions = configOptions
    }

    void setMinifyCss(boolean minifyCss) {
        pipelineExtension.minifyCss = minifyCss
    }

    @InputFiles
    @Optional
    public FileCollection getClasspath() {
        try {
            return getProject().configurations.getByName('runtime') as FileCollection
        } catch(e) {
            return null as FileCollection
        }
    }

    // public void setClasspath(FileCollection configuration) {
    //     this.classpath = configuration;
    // }


    @InputFiles
    FileTree getSource() {
        FileTree src = getProject().files(this.assetsDir).getAsFileTree();
        return src
    }


    @TaskAction
    @CompileDynamic
    void compile() {
        // println "Compiling assets in directory ${assetsDir}"
        def resolver = new FileSystemAssetResolver('application', assetsDir.canonicalPath)
        AssetPipelineConfigHolder.registerResolver(resolver)
        
        //Time to register Jar Resolvers
        this.getClasspath()?.files?.each { file ->
            // println "Registering Jar Resolver ${file}"
            AssetPipelineConfigHolder.registerResolver(new JarAssetResolver(file.name,file.canonicalPath,"META-INF/assets"))
            AssetPipelineConfigHolder.registerResolver(new JarAssetResolver(file.name,file.canonicalPath,"META-INF/static"))
            AssetPipelineConfigHolder.registerResolver(new JarAssetResolver(file.name,file.canonicalPath,"META-INF/resources"))
        }

        AssetPipelineConfigHolder.config = configOptions
        def assetCompiler = new AssetCompiler(pipelineExtension.toMap(),new GradleEventListener())
        assetCompiler.excludeRules.default = pipelineExtension.excludes
        assetCompiler.includeRules.default = pipelineExtension.includes
        assetCompiler.compile()
    }
}

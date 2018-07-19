package asset.pipeline.gradle

import asset.pipeline.AssetCompiler
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.AssetSpecLoader
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
    //private FileCollection classpath;

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
    boolean getEnableDigests() {
        pipelineExtension.enableDigests
    }


    void setEnableDigests(boolean enableDigests) {
        pipelineExtension.enableDigests = enableDigests
    }

    @Input
    @Optional
    boolean getMaxThreads() {
        pipelineExtension.maxThreads
    }


    void setMaxThreads(Integer maxThreads) {
        pipelineExtension.maxThreads = maxThreads
    }

	@Input
	@Optional
	String getJarTaskName() {
		pipelineExtension.jarTaskName
	}

	void setJarTaskName(String jarTaskName) {
		pipelineExtension.jarTaskName = jarTaskName
	}

    @Input
    @Optional
    boolean getEnableGzip() {
        pipelineExtension.enableGzip
    }

    void setEnableGzip(boolean enableGzip) {
        pipelineExtension.enableGzip = enableGzip
    }

    @Input
    @Optional
    boolean getEnableSourceMaps() {
        pipelineExtension.enableSourceMaps
    }

    void setEnableSourceMaps(boolean enableSourceMaps) {
        pipelineExtension.enableSourceMaps = enableSourceMaps
    }

    @Input
    @Optional
    boolean getSkipNonDigests() {
        pipelineExtension.skipNonDigests
    }

    void setSkipNonDigests(boolean skipNonDigests) {
        pipelineExtension.skipNonDigests = skipNonDigests
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

    void setMinifyCss(boolean minifyCss) {
        pipelineExtension.minifyCss = minifyCss
    }


    @Input
    @Optional
    boolean getVerbose() {
        pipelineExtension.verbose
    }

    void setVerbose(boolean verbose) {
        pipelineExtension.verbose = verbose
    }


    @Input
    @Optional
    Map getConfigOptions() {
        pipelineExtension.configOptions
    }

    void setConfig(Map configOptions) {
        pipelineExtension.configOptions = configOptions
    }


    @InputFiles
    @Optional
    public FileCollection getClasspath() {
        try {
            FileCollection runtimeFiles = getProject().configurations.getByName('runtime') as FileCollection
            
            
            FileCollection totalFiles = runtimeFiles
            try {
                FileCollection providedFiles = getProject().configurations.getByName('provided') as FileCollection
                if(providedFiles) {
                    totalFiles += providedFiles
                }    
            } catch(ex) {
                //no biggie if not there
            }
            
            try {
                FileCollection assetsFiles = getProject().configurations.getByName('assets') as FileCollection
                if(assetsFiles) {
                    totalFiles += assetsFiles 
                }
            } catch(ex2) {
                //no biggie if not there
            }
            return totalFiles
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
        pipelineExtension.resolvers.each { String path ->
            def resolverFile = project.file(path)
            if(resolverFile.exists() && resolverFile.directory) {
                src += getProject().files(path).getAsFileTree()
            }
        }
        return src
    }


    @TaskAction
    @CompileDynamic
    void compile() {
        AssetPipelineConfigHolder.config = AssetPipelineConfigHolder.config ?: [:]
        if(configOptions) {
            AssetPipelineConfigHolder.config = AssetPipelineConfigHolder.config + configOptions
        }
        AssetPipelineConfigHolder.resolvers = []
        registerResolvers()     
        loadAssetSpecifications()
        
        def listener = verbose ? new GradleEventListener() : null
        def assetCompiler = new AssetCompiler(pipelineExtension.toMap(), listener)
        assetCompiler.excludeRules.default = pipelineExtension.excludes
        assetCompiler.includeRules.default = pipelineExtension.includes
        assetCompiler.compile()
    }

    void registerResolvers() {
        def mainFileResolver = new FileSystemAssetResolver('application', assetsDir.canonicalPath)
        AssetPipelineConfigHolder.registerResolver(mainFileResolver)

        pipelineExtension.resolvers.each { String path ->
            File resolverFile = project.file(path)
            boolean isJarFile = resolverFile.exists() && resolverFile.file && resolverFile.name.endsWith('.jar')
            boolean isAssetFolder = resolverFile.exists() && resolverFile.directory
            if (isJarFile) {
                registerJarResolvers(resolverFile)
            }
            else if (isAssetFolder) {
                def fileResolver = new FileSystemAssetResolver(path, resolverFile.canonicalPath, false)
                AssetPipelineConfigHolder.registerResolver(fileResolver)
            }
        }

        this.getClasspath()?.files?.each { registerJarResolvers(it) }
    }
    
    void registerJarResolvers(File jarFile) {
        def isJarFile = jarFile.name.endsWith('.jar') || jarFile.name.endsWith('.zip')
        if (jarFile.exists() && isJarFile) {
            AssetPipelineConfigHolder.registerResolver(new JarAssetResolver(jarFile.name, jarFile.canonicalPath, 'META-INF/assets'))
            AssetPipelineConfigHolder.registerResolver(new JarAssetResolver(jarFile.name, jarFile.canonicalPath, 'META-INF/static'))
            AssetPipelineConfigHolder.registerResolver(new JarAssetResolver(jarFile.name, jarFile.canonicalPath, 'META-INF/resources'))
        }
    }
    
    void loadAssetSpecifications() {
        Set<File> processorFiles = project.configurations.getByName(AssetPipelinePlugin.ASSET_CONFIGURATION_NAME)?.files

        if (processorFiles) {
            URL[] urls = processorFiles.collect { it.toURI().toURL() }
            ClassLoader classLoader = new URLClassLoader(urls as URL[], getClass().classLoader)
            AssetSpecLoader.loadSpecifications(classLoader)
        }
        else {
            AssetSpecLoader.loadSpecifications()
        }
    }
}

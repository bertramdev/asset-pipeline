/*
 * Copyright 2014-2025 the original author or authors.
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
package asset.pipeline

import asset.pipeline.fs.ClasspathAssetResolver
import asset.pipeline.fs.FileSystemAssetResolver
import asset.pipeline.grails.AssetMethodTagLib
import asset.pipeline.grails.AssetPipelineBeanDefinitionRegistrar
import asset.pipeline.grails.AssetProcessorService
import asset.pipeline.grails.AssetSupportingCachingLinkGenerator
import asset.pipeline.grails.AssetSupportingLinkGenerator
import asset.pipeline.grails.AssetsTagLib
import grails.compiler.beans.GrailsBeans
import grails.config.Settings
import grails.core.GrailsApplication
import grails.plugins.Plugin
import grails.util.BuildSettings
import grails.util.Environment
import grails.web.mapping.LinkGenerator
import groovy.util.logging.Slf4j
import org.grails.config.NavigableMap
import org.grails.plugins.BinaryGrailsPlugin
import org.springframework.beans.factory.BeanRegistrar
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication

import java.util.function.Consumer
import java.util.function.Function

@Slf4j
@GrailsBeans
// An ordering hint, not a dependency: everything used below comes from grails-web-url-mappings,
// while the class named lives in grails-url-mappings, which this plugin declares compileOnly.
@AutoConfiguration(beforeName = 'org.grails.plugins.web.mapping.UrlMappingsAutoConfiguration')
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class AssetPipelineGrailsPlugin extends Plugin {

    def grailsVersion = '8.0.0 > *'
    def title = 'Asset Pipeline Plugin'
    def author = 'David Estes'
    def description = 'The Asset-Pipeline is a plugin used for managing and processing static assets in Grails applications. Asset-Pipeline functions include processing and minification of both CSS and JavaScript files. It is also capable of being extended to compile custom static assets, such as CoffeeScript.'
    def documentation = 'https://wondrify.github.io/asset-pipeline/'
    def license = 'APACHE'
    def organization = [name: 'Bertram Capital', url: 'https://www.bertramcapital.com/']
    def issueManagement = [system: 'GITHUB', url: 'https://github.com/wondrify/asset-pipeline/issues']
    def scm = [url: 'https://github.com/wondrify/asset-pipeline']
    def pluginExcludes = [
            'grails-app/assets/**',
            'test/dummy/**'
    ]
    def developers = [[name: 'Brian Wheeler']]
    def loadAfter = ['url-mappings']


    /**
     * The beans this plugin contributes, compiled into a sibling AssetPipelineAutoConfiguration.
     *
     * <p>That sibling is a plain auto-configuration listed in AutoConfiguration.imports, so it is
     * read wherever Spring Boot reads auto-configurations - including an application that renders
     * GSP without being a Grails application, which runs no plugin lifecycle at all. Each bean
     * carries the condition that says which of the two it belongs to.
     */
    def beans = {

        field('cacheUrls', Boolean).value('${' + Settings.WEB_LINK_GENERATOR_USE_CACHE + ':#{null}}')
        field('serverURL', String).value('${' + Settings.SERVER_URL + ':#{null}}')

        bean(AssetProcessorService).conditionalOnMissingBean()

        // Only where Grails itself is running: a standalone GSP application has GSP's own tag
        // library lookup, and no url mappings holder for DefaultLinkGenerator to autowire.
        bean('grailsLinkGenerator', LinkGenerator)
                .annotate(ConditionalOnMissingBean, type: 'org.grails.web.pages.StandaloneTagLibraryLookup')
                { AssetProcessorService assetProcessorService ->
                    boolean useCache = cacheUrls == null ?
                            !Environment.isDevelopmentMode() && !Environment.getCurrent().isReloadEnabled() :
                            cacheUrls
                    useCache ?
                            new AssetSupportingCachingLinkGenerator(serverURL, assetProcessorService) :
                            new AssetSupportingLinkGenerator(serverURL, assetProcessorService)
                }

        // The mirror of the condition above: the tag library lookup GSP registers for a standalone
        // application. A Grails application has the plugin's own lookup and finds these by scanning
        // the plugin's artefacts, so the beans it registers stay the only ones.

        bean(AssetsTagLib)
                .conditionalOnMissingBean()
                .annotate(ConditionalOnBean, value: AssetProcessorService,
                          type: 'org.grails.web.pages.StandaloneTagLibraryLookup')
                { AssetProcessorService assetProcessorService, GrailsApplication grailsApplication ->
                    AssetsTagLib tagLib = new AssetsTagLib()
                    tagLib.assetProcessorService = assetProcessorService
                    tagLib.grailsApplication = grailsApplication
                    tagLib
                }

        bean(AssetMethodTagLib)
                .conditionalOnMissingBean()
                .annotate(ConditionalOnBean, value: AssetProcessorService,
                          type: 'org.grails.web.pages.StandaloneTagLibraryLookup')
                { AssetProcessorService assetProcessorService ->
                    AssetMethodTagLib tagLib = new AssetMethodTagLib()
                    tagLib.assetProcessorService = assetProcessorService
                    tagLib
                }

        bean('assetPipelineConfiguration', InitializingBean)
                .conditionalOnMissingBeanName()
                .annotate(ConditionalOnBean, value: AssetProcessorService,
                          type: 'org.grails.web.pages.StandaloneTagLibraryLookup')
                { GrailsApplication grailsApplication ->
                    { ->
                        def configured = grailsApplication.config.getProperty('grails.assets', Map, [:])
                        AssetPipelineConfigHolder.config = configured instanceof NavigableMap ?
                                configured.toFlatConfig() : configured
                    } as InitializingBean
                }
    }

    void doWithApplicationContext() {
        //Register Plugin Paths
        def ctx = applicationContext
        AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver('application', "${BuildSettings.BASE_DIR}/grails-app/assets"))

        try {
            ctx.pluginManager.getAllPlugins()?.each { plugin ->
                if (plugin instanceof BinaryGrailsPlugin) {
                    def projectDirectory = plugin.getProjectDirectory()
                    if (projectDirectory) {
                        String assetPath = new File(plugin.getProjectDirectory(), "grails-app/assets").canonicalPath
                        AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver(plugin.name, assetPath))
                    }
                }
            }
        } catch (ex) {
            log.warn("Error loading exploded plugins ${ex}", ex)
        }
        AssetPipelineConfigHolder.registerResolver(new ClasspathAssetResolver('classpath', 'META-INF/assets', 'META-INF/assets.list'))
        AssetPipelineConfigHolder.registerResolver(new ClasspathAssetResolver('classpath', 'META-INF/static'))
        AssetPipelineConfigHolder.registerResolver(new ClasspathAssetResolver('classpath', 'META-INF/resources'))
    }

    /**
     * The pipeline settings and manifest, which are read from the application rather than
     * registered as beans. A BeanRegistrar runs pre-refresh, in the early plugin registration
     * phase, and unlike the beans DSL it can reach the plugin's own grailsApplication and
     * applicationContext.
     */
    BeanRegistrar beanRegistrar() {
        { registry, environment ->
            def application = grailsApplication
            def assetsConfig = application.config.getProperty('grails.assets', Map, [:])

            def manifestProps = new Properties()
            def manifestFile

            try {
                manifestFile = applicationContext.getResource("assets/manifest.properties")
                if (!manifestFile.exists()) {
                    manifestFile = applicationContext.getResource("classpath:assets/manifest.properties")
                }
            } catch (e) {
                if (application.warDeployed) {
                    log.warn "Unable to find asset-pipeline manifest, etags will not be properly generated"
                }
            }

            def useManifest = assetsConfig.useManifest ?: true

            if (useManifest && manifestFile?.exists()) {
                try {
                    manifestProps.load(manifestFile.inputStream)
                    assetsConfig.manifest = manifestProps
                    AssetPipelineConfigHolder.manifest = manifestProps
                } catch (e) {
                    log.warn "Failed to load Manifest"
                }
            }

            AssetPipelineConfigHolder.config = assetsConfig instanceof NavigableMap ?
                    assetsConfig.toFlatConfig() : assetsConfig

            if (BuildSettings.TARGET_DIR?.exists()) {
                AssetPipelineConfigHolder.config['cacheLocation'] =
                        new File(BuildSettings.TARGET_DIR, CacheManager.CACHE_LOCATION).canonicalPath
            }

            registry.registerBean('assetPipelineBeanDefinitionRegistrar',
                    AssetPipelineBeanDefinitionRegistrar, { spec ->
                        spec.infrastructure()
                        spec.supplier({ context ->
                            new AssetPipelineBeanDefinitionRegistrar(assetsConfig)
                        } as Function)
                    } as Consumer)
        } as BeanRegistrar
    }
}

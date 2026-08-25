/*
 * Copyright 2026 the original author or authors.
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
package asset.pipeline.grails

import asset.pipeline.AssetPipelineAutoConfiguration
import asset.pipeline.AssetPipelineConfigHolder

import grails.core.DefaultGrailsApplication
import grails.core.GrailsApplication
import grails.web.mapping.UrlMappingsHolder
import org.grails.web.mapping.DefaultUrlMappingsHolder
import org.grails.web.pages.StandaloneTagLibraryLookup
import org.springframework.beans.BeanUtils
import org.springframework.boot.test.context.runner.WebApplicationContextRunner

import spock.lang.Specification

/**
 * What this plugin offers an application that renders GSP views without being a Grails application:
 * the tag libraries as beans of its context, found by the tag library lookup GSP registers for it.
 *
 * <p>No plugin lifecycle runs here - no plugin manager, no artefact scanning - which is the whole
 * point. If a future change makes the tag libraries depend on any of that, this fails.
 */
class AssetPipelineTagLibAutoConfigurationSpec extends Specification {

    void 'the auto-configuration is one Spring Boot will find'() {
        given: 'the tests below import the class themselves, so none of them would notice its absence here'
        String imports = getClass().getResourceAsStream(
                '/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports').text

        expect:
        imports.readLines()*.trim().contains(AssetPipelineTagLibAutoConfiguration.name)
    }

    void cleanup() {
        AssetPipelineConfigHolder.config = [:]
    }

    private WebApplicationContextRunner contextRunner() {
        new WebApplicationContextRunner()
                .withUserConfiguration(AssetPipelineAutoConfiguration)
                .withBean('assetProcessorService', AssetProcessorService, () -> new AssetProcessorService())
    }

    /** What GSP contributes to an application it is auto-configured for. */
    private WebApplicationContextRunner standaloneGsp() {
        contextRunner()
                .withBean('gspTagLibraryLookup', StandaloneTagLibraryLookup,
                        () -> BeanUtils.instantiateClass(StandaloneTagLibraryLookup))
                .withBean('grailsApplication', GrailsApplication, () -> new DefaultGrailsApplication())
    }

    void 'the tag libraries are found by the lookup a standalone GSP application registers'() {
        expect:
        standaloneGsp().run { context ->
            StandaloneTagLibraryLookup lookup = context.getBean(StandaloneTagLibraryLookup)
            // what a page rendering <asset:stylesheet> needs to resolve
            assert lookup.lookupTagLibrary('asset', 'stylesheet') instanceof AssetsTagLib
            assert lookup.lookupTagLibrary('g', 'assetPath') instanceof AssetMethodTagLib
        }
    }

    void 'the settings an application writes reach the pipeline itself'() {
        expect:
        standaloneGsp().withPropertyValues('grails.assets.mapping=static-assets').run { context ->
            context.getBean(AssetsTagLib)
            // read by the pipeline, rather than the default it would never have heard otherwise
            assert AssetPipelineConfigHolder.config['mapping'] == 'static-assets'
        }
    }

    void 'a Grails application is left to the plugin'() {
        expect: 'no standalone lookup, which is what a Grails application has'
        contextRunner()
                .withBean('grailsApplication', GrailsApplication, () -> new DefaultGrailsApplication())
                // the merged auto-configuration also contributes grailsLinkGenerator here, which a
                // real Grails application has this from grails-url-mappings
                .withBean('grailsUrlMappingsHolder', UrlMappingsHolder, () -> new DefaultUrlMappingsHolder([]))
                .run { context ->
                    assert !context.containsBean('assetsTagLib')
                    assert !context.containsBean('assetMethodTagLib')
                }
    }

}

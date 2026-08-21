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
package asset.pipeline.springboot

import asset.pipeline.grails.AssetMethodTagLib
import asset.pipeline.grails.AssetProcessorService
import asset.pipeline.grails.AssetsTagLib
import grails.core.DefaultGrailsApplication
import grails.core.GrailsApplication
import org.grails.taglib.TagLibraryLookup
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import spock.lang.Specification

/**
 * What a Spring Boot application rendering its views with GSP gets from this library: the tag
 * libraries the Grails plugin carries, as beans of its context.
 */
class AssetPipelineTagLibAutoConfigurationSpec extends Specification {

    private WebApplicationContextRunner contextRunner() {
        new WebApplicationContextRunner()
                .withUserConfiguration(AssetPipelineTagLibAutoConfiguration)
    }

    private WebApplicationContextRunner withAssetPipeline() {
        contextRunner()
                .withBean('assetProcessorService', AssetProcessorService, () -> new AssetProcessorService())
                .withBean('grailsApplication', GrailsApplication, () -> new DefaultGrailsApplication())
                // what GSP contributes to an application that renders with it
                .withBean('tagLibraryLookup', TagLibraryLookup, () -> new TagLibraryLookup())
    }

    void 'the tag libraries are beans of an application that has the asset pipeline'() {
        expect:
        withAssetPipeline().run { context ->
            assert context.getBean(AssetsTagLib).assetProcessorService != null
            assert context.getBean(AssetsTagLib).grailsApplication != null
            assert context.getBean(AssetMethodTagLib).assetProcessorService != null
        }
    }

    void 'an application without the asset pipeline service gets neither'() {
        expect: 'the service is auto-configured by the plugin itself, so nothing is wired without it'
        contextRunner().withBean('tagLibraryLookup', TagLibraryLookup, () -> new TagLibraryLookup()).run { context ->
            assert !context.containsBean('assetsTagLib')
            assert !context.containsBean('assetMethodTagLib')
        }
    }

    void 'a tag library the application declares itself is left alone'() {
        given:
        AssetsTagLib ownTagLib = new AssetsTagLib()

        expect:
        withAssetPipeline().withBean('assetsTagLib', AssetsTagLib, () -> ownTagLib).run { context ->
            assert context.getBean(AssetsTagLib).is(ownTagLib)
        }
    }

}

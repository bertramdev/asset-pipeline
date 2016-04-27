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

package asset.pipeline.grails

import grails.test.spock.IntegrationSpec
import asset.pipeline.AssetPipelineConfigHolder

class LinkGeneratorSpec extends IntegrationSpec {
    def grailsApplication
    def assetProcessorService

    def "finds assets when calling for resource in dev mode"() {
        given: "A LinkGenerator and an image"
            AssetPipelineConfigHolder.manifest = null
            def linkGenerator = new LinkGenerator("http://localhost:8080")
            linkGenerator.assetProcessorService = assetProcessorService

            def filePath = "grails_logo.png"
        when:
            def resource = linkGenerator.resource(file: filePath)
        then:
            resource == "/assets/grails_logo.png"
    }


    def "finds assets with absolute path when calling for resource in dev mode"() {
        given: "A LinkGenerator and an image"
            AssetPipelineConfigHolder.manifest = null
            def linkGenerator = new LinkGenerator("http://localhost:8080")
            linkGenerator.assetProcessorService = assetProcessorService

            def filePath = "grails_logo.png"
        when:
            def resource = linkGenerator.resource(file: filePath,absolute:true)
        then:
            resource == "http://localhost:8080/assets/grails_logo.png"
    }

    def "find asset path only with no file"() {
        given: "A LinkGenerator pointed to a directory"
           AssetPipelineConfigHolder.manifest = null
            def linkGenerator = new LinkGenerator("http://localhost:8080")
            linkGenerator.assetProcessorService = assetProcessorService
        when:
            def resource = linkGenerator.resource(dir: 'asset-pipeline/test')
        then:
            resource != null
    }

    def "finds asset in precompiled (prod) mode"() {
        given: "A LinkGenerator and an image"
            def linkGenerator = new LinkGenerator("http://localhost:8080")
            linkGenerator.assetProcessorService = assetProcessorService
            def filePath = "grails_logo.png"
            Properties manifestProperties = new Properties()
            manifestProperties.setProperty(filePath, "grails_logo-abcdefg.png")
            grailsApplication.config.grails.assets.manifest = manifestProperties
            AssetPipelineConfigHolder.manifest = manifestProperties

        when:
            def resource = linkGenerator.resource(file: filePath)
        then:
            resource == "/assets/grails_logo-abcdefg.png"
    }

    def "falls back to standard resource lookup if not found in asset pipeline"() {
        given: "A LinkGenerator and an image"
            def linkGenerator = new LinkGenerator("http://localhost:8080")
            linkGenerator.assetProcessorService = assetProcessorService

            def filePath = "fake_image.png"
        when:
            def resource = linkGenerator.resource(file: filePath)
        then:
            resource == "/fake_image.png"
    }
}
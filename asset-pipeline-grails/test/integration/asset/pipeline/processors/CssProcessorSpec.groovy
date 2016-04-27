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

package asset.pipeline.processors

import grails.test.spock.IntegrationSpec
import grails.util.Holders
import asset.pipeline.AssetCompiler
import asset.pipeline.fs.FileSystemAssetResolver

class CssProcessorSpec extends IntegrationSpec {

    def "replaces image urls with relative paths"() {
        given: "some css and a CssProcessor"
            def cssProcessor = new CssProcessor(null)
            def resolver = new FileSystemAssetResolver('application','grails-app/assets')
            def assetFile = resolver.getAsset('asset-pipeline/test/test.css','text/css')

        when:
            def processedCss = cssProcessor.process(assetFile.inputStream.text, assetFile)
        then:
            processedCss.contains("url('../../grails_logo.png')")
    }

    def "replaces image urls with relative paths and cache digest names in precompiler mode"() {
        given: "some css and a CssProcessor"

            def cssProcessor = new CssProcessor(new AssetCompiler())
            def resolver = new FileSystemAssetResolver('application','grails-app/assets')
            def assetFile = resolver.getAsset('asset-pipeline/test/test.css','text/css')
            Holders.metaClass.static.getConfig = { ->
                [grails: [assets: [[minifyJs: true]]]]
            }
        when:
            def processedCss = cssProcessor.process(assetFile.inputStream.text, assetFile)
        then:
            processedCss.contains("url('../../grails_logo-eabe4af98753b0163266d7e68bbd32e3.png')")
    }
}

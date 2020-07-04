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


import asset.pipeline.AssetPipeline
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.FileSystemAssetResolver
import spock.lang.Specification
import grails.testing.services.ServiceUnitTest
/**
 * @author Tommy Barker
 */
class AssetProcessorServiceSpec extends Specification implements ServiceUnitTest<AssetProcessorService> {

	private static final MOCK_BASE_SERVER_URL = 'http://localhost:8080/foo'


	def setup() {
		AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver('application','grails-app/assets'))
		service.grailsLinkGenerator = [serverBaseURL: MOCK_BASE_SERVER_URL]
	}

	void "asset mapping can be configured"() {
		given:
			def path
			final def goodConfig = new ConfigObject()
			goodConfig.grails.assets.mapping = "foo"
			final def badConfig = new ConfigObject()
			badConfig.grails.assets.mapping = "foo/bar"

		when: "retrieving mapping with no configuration"
			path = service.assetMapping
		then:
			"assets" == path
			noExceptionThrown()

		when: "mapping set to 'foo'"
			service.grailsApplication.config = goodConfig
			path = service.assetMapping
		then:
			"foo" == path
			noExceptionThrown()

		when: "mapping set to 'foo/bar'"
			service.grailsApplication.config = badConfig
			service.assetMapping
		then: "error is thrown since only one level is supported"
			thrown(IllegalArgumentException)
	}

	void "can get flattened dependency list"() {
		given:
			final def fileUri = "asset-pipeline/test/test"
			final def extension = "js"
			final def contentType = "application/javascript"
			def depList

		when:
			depList = AssetPipeline.getDependencyList(fileUri, contentType, extension)
		then:
			depList?.size() > 0

		when:
			depList = AssetPipeline.getDependencyList("unknownfile", contentType, extension)
		then:
			depList == null
	}

	void "can serve unprocessed asset for dev debug"() {
		given:
			final def fileUri = "asset-pipeline/test/test"
			final def extension = "js"
			final def contentType = "application/javascript"
			def uncompiledFile

		when:
			uncompiledFile = AssetPipeline.serveUncompiledAsset(fileUri, contentType, extension)
		then:
			!(new String(uncompiledFile)).contains("This is File A")

		when:
			uncompiledFile = AssetPipeline.serveUncompiledAsset('unknownfile', contentType, extension)
		then:
			uncompiledFile == null
	}

	void "can serve compiled assets"() {
		given:
			final def fileUri = "asset-pipeline/test/test"
			final def extension = "js"
			final def contentType = "application/javascript"
			def uncompiledFile

		when:
			uncompiledFile = AssetPipeline.serveAsset(fileUri, contentType, extension)
		then:
			(new String(uncompiledFile)).contains("This is File A")

		when:
			uncompiledFile = AssetPipeline.serveAsset('unknownfile', contentType, extension)
		then:
			uncompiledFile == null
	}
}

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

class AssetResourceLocatorSpec extends IntegrationSpec {
	def grailsApplication
	def assetResourceLocator

	def "finds assets when calling for resource in dev mode"() {
		given: "An AssetResourceLocator"
			def filePath = "asset-pipeline/test/test.js"
		when:
			def resource = assetResourceLocator.findResourceForURI(filePath)
		then:
			resource?.exists() == true
			resource.inputStream.text.contains("This is File A")
	}

	def "finds asset of image type when calling for resource in dev mode"() {
		given: "An AssetResourceLocator"
			def filePath = "grails_logo.png"
		when:
			def resource = assetResourceLocator.findResourceForURI(filePath)
		then:
			resource?.exists() == true
	}
}

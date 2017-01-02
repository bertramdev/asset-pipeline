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

package asset.pipeline.fs

import asset.pipeline.*
import spock.lang.Specification

/**
* @author David Estes
*/
class JarAssetResolverSpec extends Specification {

	void "should be able to fetch files from a jar file"() {
		given:
			def resolver = new JarAssetResolver('application','lib/test-lib.zip','META-INF/assets')
		when:
			def file = resolver.getAsset('jartest','application/javascript')
		then:
			println file?.inputStream?.text
			file instanceof JsAssetFile
	}

	void "should be able to fetch files from a jar file if root path given"() {
		given:
			def resolver = new JarAssetResolver('application','lib/test-lib.zip','META-INF/assets')
		when:
			def file = resolver.getAsset('/jartest','application/javascript')
		then:
			println file?.inputStream?.text
			file instanceof JsAssetFile
	}


	void "should load exact directory and not all directories with the same prefixes"() {
		given:
			def resolver = new JarAssetResolver('application','lib/test-lib.zip','META-INF/assets')
		when:
			def files = resolver.getAssets('jquery','application/javascript')
		then:
            files.name == ['jquery.js']
	}
}

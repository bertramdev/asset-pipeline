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


import asset.pipeline.JsEs6AssetFile
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
			file instanceof JsEs6AssetFile
	}

	void "should be able to fetch files from a jar file if root path given"() {
		given:
			def resolver = new JarAssetResolver('application','lib/test-lib.zip','META-INF/assets')
		when:
			def file = resolver.getAsset('/jartest','application/javascript')
		then:
			println file?.inputStream?.text
			file instanceof JsEs6AssetFile
	}


	void "should load exact directory and not all directories with the same prefixes"() {
		given:
			def resolver = new JarAssetResolver('application','lib/test-lib.zip','META-INF/assets')
		when:
			def files = resolver.getAssets('jquery','application/javascript')
		then:
            files.name == ['jquery.js']
	}

	void "should prefer .js file over .mjs file when explicitly requesting .js extension from jar"() {
		given:
		def resolver = new JarAssetResolver('application','lib/test-lib.zip','META-INF/assets')

		when:
		def file = resolver.getAsset('jartest.js', 'application/javascript')

		then:
		file != null
		file.name == 'jartest.js'
		file.path.endsWith('.js')
	}

	void "should prefer .mjs file over .js file when explicitly requesting .mjs extension from jar"() {
		given:
		def resolver = new JarAssetResolver('application','lib/test-lib.zip','META-INF/assets')

		when:
		def file = resolver.getAsset('jartest.mjs', 'application/javascript')

		then:
		file != null
		file.name == 'jartest.mjs'
		file.path.endsWith('.mjs')
	}

	void "should prefer .mjs file over .js when no extension is specified"() {
		given:
		def resolver = new JarAssetResolver('application','lib/test-lib.zip','META-INF/assets')

		when:
		def jsFile = resolver.getAsset('jartest', 'application/javascript')

		then:
		jsFile != null
		jsFile instanceof JsEs6AssetFile
		jsFile.name == 'jartest.mjs'
		jsFile.path.endsWith('.mjs')
	}
}

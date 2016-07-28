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

package asset.pipeline

import spock.lang.Specification
import asset.pipeline.fs.FileSystemAssetResolver
/**
 * @author David Estes
 */
class AssetHelperSpec extends Specification {
    def setup() {
        AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver('application','assets'))
    }


    void "should get file specs by contentType"() {
    	when:
    		def assetHelper = AssetHelper
    	then:
	    	fileSpecs as Set == assetHelper.getPossibleFileSpecs(contentType) as Set
	    where:
	    	contentType                | fileSpecs
	    	'application/javascript'   | [JsAssetFile, JsEs6AssetFile]
	    	'application/x-javascript' | [JsAssetFile, JsEs6AssetFile]
	    	'text/javascript'          | [JsAssetFile, JsEs6AssetFile]
	    	'text/css'                 | [CssAssetFile]
	    	'text/html'                | [HtmlAssetFile]
	    	'blob/text'                | []
    }

    void "Should Resolve asset:// URL Protocol Spec"() {
        given:
            def url = new java.net.URL("asset:///asset-pipeline/test/test.css")
        when:
            def results = url.text
        then:
            results.contains('#logo')
    }

    void "should get asset file object based on file name and extension" () {
    	given:
            def testFileName = 'asset-pipeline/test/test'
            def testFileExt = "css"
            def assetFile
        when:
        	assetFile = AssetHelper.getAssetFileWithExtension(testFileName, testFileExt)
        then:
        	assetFile?.name == 'test.css'

        when:
	        testFileName = 'asset-pipeline/test/test'
            testFileExt = "sass"
        	assetFile = AssetHelper.getAssetFileWithExtension(testFileName, testFileExt)
        then:
        	assetFile == null

        when:
	        testFileName = 'asset-pipeline/test/test.css'
	        testFileExt = null
        	assetFile = AssetHelper.getAssetFileWithExtension(testFileName, testFileExt)
        then:
        	assetFile?.name == 'test.css'
    }

    void "should provide file name without the extension"() {
    	given:
	    	def testName = "test.min.js"
	    	def nameWithoutExt
    	when:
    		nameWithoutExt = AssetHelper.nameWithoutExtension(testName)
    	then:
    		nameWithoutExt == 'test.min'
    }

    void "should extract extension from file name"() {
    	given:
	    	def testName = "test.min.js"
	    	def ext
    	when:
    		ext = AssetHelper.extensionFromURI(testName)
    	then:
    		ext == 'js'
    }

    void "should normalize paths"() {
        when:
        def result = AssetHelper.normalizePath(path)
        then:
        result == normalizedPath
        where:
        path                      | normalizedPath
        'images/../test.png'      | 'test.png'
        '../fonts/test.eot'       | 'test.eot'
    }
}

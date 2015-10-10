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

package asset.pipeline.ember

import asset.pipeline.handlebars.*
import spock.lang.Specification

/**
* @author David Estes
*/
class EmberHandlebarsProcessorSpec extends Specification {

	void "should compile handlebars into js using rhino"() {
		given:
		def handlebarsText = '''
		<html>
		<body>
		<h1>{{title}}</h1>
		</body>
		</html>
		'''
		def assetFile = new HandlebarsAssetFile()
		assetFile.path = "templates/test.handlebars"
		def processor = new EmberHandlebarsProcessor()
		when:
		def output = processor.process(handlebarsText, assetFile)
		then:
		output.contains('Ember.TEMPLATES[\'test\']')
	}



}

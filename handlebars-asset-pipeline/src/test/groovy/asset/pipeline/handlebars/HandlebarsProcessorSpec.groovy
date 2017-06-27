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

package asset.pipeline.handlebars

import spock.lang.Specification
import asset.pipeline.AssetPipelineConfigHolder

/**
* @author David Estes
*/
class HandlebarsProcessorSpec extends Specification {

	void "should compile handlebars into js using rhino"() {
		given:
		def handlebarsText = '''
		<html>
		<body>
			<h1>{{title}}</h1>
		</body>
		</html>
		'''
		AssetPipelineConfigHolder.config = [:]
		def assetFile = new HandlebarsAssetFile()
		assetFile.path = "templates/test.handlebars"
		def processor = new HandlebarsProcessor()
		when:
		def output = processor.process(handlebarsText, assetFile)
		then:
		output.contains('templates[\'test\']')
	}

	void "should compile handlebars into js with nested template name"() {
		given:
		def handlebarsText = '''
		<html>
		<body>
			<h1>{{title}}</h1>
		</body>
		</html>
		'''
		AssetPipelineConfigHolder.config = [:]
		def assetFile = new HandlebarsAssetFile()
		assetFile.path = "templates/sub/test.handlebars"
		def processor = new HandlebarsProcessor()
		when:
		def output = processor.process(handlebarsText, assetFile)
		then:
		output.contains('templates[\'sub/test\']')
	}

	void "should handle compiling templates with no parent path"() {
				given:
		def handlebarsText = '''
		<html>
		<body>
			<h1>{{title}}</h1>
		</body>
		</html>
		'''
		AssetPipelineConfigHolder.config = [:]
		def assetFile = new HandlebarsAssetFile()
		assetFile.path = "test.handlebars"
		def processor = new HandlebarsProcessor()
		when:
		def output = processor.process(handlebarsText, assetFile)
		then:
		output.contains('templates[\'test\']')
	}

	void "should compile handlebars into js with nested template name of not part of template root"() {
		given:
		def handlebarsText = '''
		<html>
		<body>
			<h1>{{title}}</h1>
		</body>
		</html>
		'''
		AssetPipelineConfigHolder.config = [:]
		def assetFile = new HandlebarsAssetFile()
		assetFile.path = "sub/test.handlebars"
		def processor = new HandlebarsProcessor()
		when:
		def output = processor.process(handlebarsText, assetFile)
		then:
		output.contains('templates[\'sub/test\']')
	}

	void "should compile handlebars into js with nested template name if nested template name contains root name"() {
		given:
		def handlebarsText = '''
		<html>
		<body>
			<h1>{{title}}</h1>
		</body>
		</html>
		'''
		AssetPipelineConfigHolder.config = [:]
		def assetFile = new HandlebarsAssetFile()
		assetFile.path = "sub/templates/test.handlebars"
		def processor = new HandlebarsProcessor()
		when:
		def output = processor.process(handlebarsText, assetFile)
		then:
		output.contains('templates[\'sub/templates/test\']')
	}


	def "should be able to use a custom wrapped template"() {
		given:
		def handlebarsText = '''
		<html>
		<body>
			<h1>{{title}}</h1>
		</body>
		</html>
		'''
		def template = '''
		  (function(){
			var template = HandlebarsCustom.template, templates = Handlebars.templates = Handlebars.templates || {};
				templates['$templateName'] = template($compiledTemplate);
		}());
		'''
		def assetFile = new HandlebarsAssetFile()
		assetFile.path = "templates/test.handlebars"
		AssetPipelineConfigHolder.config = [
			handlebars: [
				wrapTemplate: template
			]
		]
		def processor = new HandlebarsProcessor()
		when:
		def output = processor.process(handlebarsText, assetFile)
		then:
		output.contains('templates[\'test\']')
		output.contains('HandlebarsCustom')
	}


	def "should be able to registerPartial when file prefixed with _"() {
		given:
		AssetPipelineConfigHolder.config = [
			handlebars: [
				wrapTemplate: null
			]
		]
		HandlebarsProcessor.wrapTemplateCustom = null
		def handlebarsText = '''
		<html>
		<body>
			<h1>{{title}}</h1>
		</body>
		</html>
		'''
		AssetPipelineConfigHolder.config = [:]
		def assetFile = new HandlebarsAssetFile()
		assetFile.path = "templates/sub/_template.handlebars"
		def processor = new HandlebarsProcessor()

		when:
		def output = processor.process(handlebarsText, assetFile)
		then:
		output.contains('registerPartial(\'sub/template\'')
	}
}

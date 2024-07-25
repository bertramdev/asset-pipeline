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

package asset.pipeline.coffee

import spock.lang.Specification

/**
* @author David Estes
*/
class CoffeeAssetFileSpec extends Specification {
	void "should match directive patterns"() {
		given:
		def jsFile = new CoffeeAssetFile()
		when:
		def matches = (line =~ jsFile.directivePattern)?.collect{ it[1].trim()} ?: []
		def match = matches.size() > 0 ? matches[0] : null
		then:
		directive ==  match
		where:
		line 				 | directive
		'#=require_self'	 | 'require_self'
		'#= require_self'   | 'require_self'
		'#= require jquery' | 'require jquery'
		'Blank Section'      | null
		'#= require_tree .' | 'require_tree .'
	}



}

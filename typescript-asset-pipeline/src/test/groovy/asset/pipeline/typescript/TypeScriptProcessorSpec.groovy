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

package asset.pipeline.typescript

import spock.lang.Specification

/**
* @author David Estes
*/
class TypeScriptProcessorSpec extends Specification {
	void "should compile coffeescript into js using rhino"() {
		given:
			def typeScript = '''
class Person {
  public firstName: string;
  public lastName: string;
 
  constructor (firstName: string, lastName: string) {
    this.firstName = firstName;
    this.lastName = lastName;
  }
}
			'''
			TypeScriptProcessor.NODE_SUPPORTED=false
			def processor = new TypeScriptProcessor()
		when:
			def output = processor.process(typeScript, null)
		then:
			output.contains('''var Person''')
	}

	void "should compile typescript into js using node"() {
		given:
			def typeScript = '''
class Person {
  public firstName: string;
  public lastName: string;
 
  constructor (firstName: string, lastName: string) {
    this.firstName = firstName;
    this.lastName = lastName;
  }
}
			'''
			TypeScriptProcessor.NODE_SUPPORTED=true
			def processor = new TypeScriptProcessor()
		when:
			def output = processor.process(typeScript, null)
		then:
			output.contains('''var Person''')
			
	}
}

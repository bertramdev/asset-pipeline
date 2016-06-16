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

package asset.pipeline.jsx

import asset.pipeline.AssetFile
import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.jsx.symbols.*

/**
 * @author David Estes
 */
class JsxProcessor extends AbstractProcessor {

	JsxProcessor(AssetCompiler precompiler) {
		super(precompiler)

	}


	/**
	* Processes an input string from a given AssetFile implementation of coffeescript and converts it to javascript
	* @param   input String input coffee script text to be converted to javascript
	* @param   AssetFile instance of the asset file from which this file came from. Not actually used currently for this implementation.
	* @return  String of compiled javascript
	*/
	String process(String input,AssetFile  assetFile) {
		StringReader reader = new StringReader(input)
		JsxLexer lexer = new JsxLexer(reader)
		Symbol element
		ArrayList<Symbol> elements = new ArrayList<Symbol>()
		while((element = lexer.yylex()) != null) {
			elements << element
		}
		println "Received Elements ${elements} - state ${lexer.yystate()}"
		int lastPosition = 0
		StringBuilder output = new StringBuilder()
		elements.each { currElement ->
			println "looking at currElement ${currElement.name} - ${currElement.value} - ${currElement.getLength()}"
			output << input.substring(lastPosition,currElement.getPosition())
			lastPosition = currElement.getPosition() + currElement.getLength()		
			output << renderElement(currElement) + ';'
		}
		if(lastPosition < input.size()) {
			output << input.substring(lastPosition,input.size())
		}
		return output.toString()
	}

	protected String renderElement(Symbol element, Integer depth=null) {
		def reactArgs = []
		reactArgs << "\"${element.value}\""
		reactArgs << renderAttributes(element)
		depth = depth ?: element.getColumn()
		element.children?.each { child ->
			if(child instanceof JsxElement) {
				println "Child ${child.class.name} - ${child.value}"
				reactArgs << renderElement(child, depth + 2)
			} else {
				if(child.name == 'JSXText') {
					if(child.value.trim()) {
						reactArgs << "\"${child.value.trim()}\""
					}
					println "Child ${child.class.name} - ${child.name} - ${child.value}"
				} else {
					println "Child ${child.class.name} - ${child.name} - ${child.value}"
					reactArgs << child.value
				}
			}
		}

		String output = "React.createElement(\n"
		reactArgs = reactArgs.collect{
			(' ' * (depth + 2)) + it
		}
		output += reactArgs.join(",\n")
		output += "\n" + (' ' * depth) + ')'
		return output
	}

	protected String renderAttributes(Symbol element) {
		def map = [:]
		JsxAttribute spreadAttribute = element.getAttributes()?.find{it.attributeType == 'SpreadAttribute'}
		if(element.getAttributes().size() == 0) {
			return "null";
		} else if(spreadAttribute) {
			return spreadAttribute.value;
		} else {
			String out = "{"
			out += element.getAttributes().collect { attribute ->
				if(attribute.attributeType == 'assignmentExpression') {
					"${attribute.name.contains('-') ? ('"' + attribute.name + '"') : attribute.name}: ${attribute.value}"
				} else {
					"${attribute.name.contains('-') ? ('"' + attribute.name + '"') : attribute.name}: \"${attribute.value}\""
				}
				
			}.join(", ")
			out += "}"
			return out;
		}
	}
}

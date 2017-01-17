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
import java.util.regex.Pattern
import javax.swing.text.html.HTML

/**
 * @author David Estes
 */
class JsxProcessor extends AbstractProcessor {
	Pattern commentMatch = Pattern.compile("(/\\*\\*/)|(/\\*(.+?)?\\*/)", Pattern.DOTALL);

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
		StringBuilder output = new StringBuilder()
		try {
			while((element = lexer.yylex()) != null) {
				elements << element
			}
			int lastPosition = 0
			
			elements.each { currElement ->
				output << input.substring(lastPosition,currElement.getPosition())
				lastPosition = currElement.getPosition() + currElement.getLength()		
				output << renderElement(currElement,null,assetFile)
			}
			if(lastPosition < input.size()) {
				output << input.substring(lastPosition,input.size())
			}
		} catch(JsxParserException jex) {
			throw new JsxParserException("Error Parsing JSX File ${assetFile?.name}: ${jex.getMessage()} State: ${lexer.yystate()}")
		}
		
		return output.toString()
	}

	protected String renderElement(Symbol element, Integer depth=null, AssetFile assetFile=null) {
		def reactArgs = []
		reactArgs << elementNameForValue(element.value)
		reactArgs << renderAttributes(element)
		depth = depth ?: element.getColumn()
		element.children?.each { child ->
			if(child instanceof JsxElement) {
				reactArgs << renderElement(child, depth + 2, assetFile)
			} else {
				if(child.name == 'JSXText') {
					if(child.value.trim()) {
						List finalValue = []
						Integer lineNo = 0
						child.value.eachLine { line ->
							if(line.trim()) {
								if(lineNo == 0) {
									finalValue << rtrim(line)
								} else {
									finalValue << line.trim()
								}
							}
							lineNo++
						}
						if(finalValue) {
							reactArgs << "\"${finalValue.join(" ").replace('"','\\\"')	}\""
						}
					}
				} else {
					
					child.value = child.value.replaceAll(commentMatch,"") //strip comments
					if(child.value.trim()) {
						reactArgs << process(child.value.trim(),assetFile)
					}
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
		JsxAttribute spreadAttribute = element.getAttributes()?.find{it.attributeType == 'spreadAttribute'}
		if(element.getAttributes().size() == 0) {
			return "null";
		} else if(spreadAttribute) {
			return spreadAttribute.value;
		} else {
			String out = "{"
			out += element.getAttributes().collect { attribute ->
				if(attribute.attributeType == 'assignmentExpression') {
					"${attribute.name.contains('-') ? ('"' + attribute.name + '"') : attribute.name}: ${attribute.value}"
				} else if(!attribute.value) {
					"${attribute.name.contains('-') ? ('"' + attribute.name + '"') : attribute.name}: true"
				} else {
					"${attribute.name.contains('-') ? ('"' + attribute.name + '"') : attribute.name}: \"${attribute.value}\""
				}
				
			}.join(", ")
			out += "}"
			return out;
		}
	}


	// protected attributeNameFromCamel(String value) {
	// 	return value.replaceAll(/\B[A-Z]/) { '-' + it }.toLowerCase() 
	// }
	protected elementNameForValue(String value) {
		if(HTML_ELEMENTS.contains(value)) {
			return "\"${value}\""
		} else if(value.toLowerCase() == value) {
			return "\"${value}\""
		} else {
			return value
		}
	}

	public static String rtrim(String s) {
        int i = s.length()-1;
        while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
            i--;
        }
        return s.substring(0,i+1);
    }


	static List<String> HTML_ELEMENTS = [
		'a', 'abbr', 'address', 'area', 'article', 'aside', 'audio', 'b', 'base', 'bdi', 'bdo', 'big', 'blockquote', 'body', 'br', 'button', 'canvas', 'caption', 'cite', 'code', 'col', 'colgroup', 'data', 'datalist', 'dd', 'del', 'details', 'dfn', 'dialog', 'div', 'dl', 'dt', 'em', 'embed', 'fieldset', 'figcaption', 'figure', 'footer', 'form', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'head', 'header', 'hgroup', 'hr', 'html', 'i', 'iframe', 'img', 'input', 'ins', 'kbd', 'keygen', 'label', 'legend', 'li', 'link', 'main', 'map', 'mark', 'menu', 'menuitem', 'meta', 'meter', 'nav', 'noscript', 'object', 'ol', 'optgroup', 'option', 'output', 'p', 'param', 'picture', 'pre', 'progress', 'q', 'rp', 'rt', 'ruby', 's', 'samp', 'script', 'section', 'select', 'small', 'source', 'span', 'strong', 'style', 'sub', 'summary', 'sup', 'table', 'tbody', 'td', 'textarea', 'tfoot', 'th', 'thead', 'time', 'title', 'tr', 'track', 'u', 'ul', 'var', 'video', 'wbr', 'circle', 'clipPath', 'defs', 'ellipse', 'g', 'image', 'line', 'linearGradient', 'mask', 'path', 'pattern', 'polygon', 'polyline', 'radialGradient', 'rect', 'stop', 'svg', 'text', 'tspan'
	]
}

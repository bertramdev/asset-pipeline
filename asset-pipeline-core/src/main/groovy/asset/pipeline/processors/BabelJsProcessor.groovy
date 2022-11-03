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

package asset.pipeline.processors

import asset.pipeline.AssetFile
import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.JsAssetFile
import asset.pipeline.AssetPipelineConfigHolder
import groovy.util.logging.Slf4j

import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess

// CoffeeScript engine will attempt to use Node.JS coffee if it is available on
// the system path. If not, it uses Mozilla Rhino to compile the CoffeeScript
// template using the javascript in-browser compiler.
@Slf4j
class BabelJsProcessor extends AbstractProcessor {

	static Boolean NODE_SUPPORTED
	ClassLoader classLoader

	static Context context
	static def bindings
	private static final $LOCK = new Object[0]
	BabelJsProcessor(AssetCompiler precompiler) {
		super(precompiler)
	
		
		
	}

	protected void loadBabelJs() {
		if(!context) {
			synchronized($LOCK) {
				if(!context) {
					def babelJsResource = classLoader.getResource('asset/pipeline/babel.min.js')
					context = Context.newBuilder().allowExperimentalOptions(true).allowHostAccess(HostAccess.newBuilder().allowListAccess(true).allowMapAccess(true).allowArrayAccess(true).build()).build()

					context.eval("js",babelJsResource.getText('UTF-8'))
					def presets = "{ \"presets\": [\"es2015\",[\"stage-2\",{\"decoratorsLegacy\": true}],\"react\"], \"compact\": false }"
					if(AssetPipelineConfigHolder.config?.babel?.options) {
						presets = AssetPipelineConfigHolder.config?.babel?.options
					}
					bindings = context.getBindings("js")
					
					bindings.putMember("optionsJson", presets);
					context.eval("js","var options = JSON.parse(optionsJson);");

				}
			}
		}

	}


	/**
	* Processes an input string from a given AssetFile implementation of coffeescript and converts it to javascript
	* @param   input String input coffee script text to be converted to javascript
	* @param   AssetFile instance of the asset file from which this file came from. Not actually used currently for this implementation.
	* @return  String of compiled javascript
	*/
	String process(String input,AssetFile  assetFile) {
		if(!input) {
			return input
		}
		Boolean newEcmascriptKeywordsFound = false
		if(input.contains("export default")) {
			newEcmascriptKeywordsFound = true;
		}
		if(assetFile instanceof JsAssetFile) {
			if((!newEcmascriptKeywordsFound && !AssetPipelineConfigHolder.config?.enableES6 && !AssetPipelineConfigHolder.config?."enable-es6") || (newEcmascriptKeywordsFound && (AssetPipelineConfigHolder.config?.enableES6 == false || AssetPipelineConfigHolder.config?."enable-es6" == false))) {
				return input
			}
		}
		try {
			classLoader = getClass().getClassLoader()
			//cx.setOptimizationLevel(-1)
			//globalScope = cx.initStandardObjects()
			loadBabelJs()
		} catch(Exception e) {
			throw new Exception("BabelJs Engine initialization failed.", e)
		} finally {
			try {
			} catch(IllegalStateException e) {
			}
		}
		try {
			

			synchronized($LOCK) {
				bindings.putMember("input", input);
				def result = context.eval("js","Babel.transform(input, options).code");
				return result
			}
		} catch(Exception e) {
			throw new Exception("""BabelJs Engine compilation of javascript failed.
			$e
			""",e)
		} finally {

		}
	}



}

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

import asset.pipeline.AssetFile
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler

// CoffeeScript engine will attempt to use Node.JS coffee if it is available on
// the system path. If not, it uses Mozilla Rhino to compile the CoffeeScript
// template using the javascript in-browser compiler.
class CoffeeScriptProcessor extends AbstractProcessor {

	static Boolean NODE_SUPPORTED
	Scriptable globalScope
	ClassLoader classLoader

	CoffeeScriptProcessor(AssetCompiler precompiler) {
		super(precompiler)
		if(!isNodeSupported()) {
			try {
				classLoader = getClass().getClassLoader()
				def coffeeScriptJsResource = classLoader.getResource('asset/pipeline/coffee/coffee-script-1.7.1.js')




				Context cx = Context.enter()
				cx.setOptimizationLevel(-1)
				globalScope = cx.initStandardObjects()
				cx.evaluateString globalScope, coffeeScriptJsResource.getText("UTF-8"), coffeeScriptJsResource.file, 1, null
			} catch(Exception e) {
				throw new Exception("CoffeeScript Engine initialization failed.", e)
			} finally {
				try {
					Context.exit()
				} catch(IllegalStateException e) {
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
		if(isNodeSupported()) {
			return processWithNode(input, assetFile)
		}
		else {
			try {
				def cx = Context.enter()
				def compileScope = cx.newObject(globalScope)
				compileScope.setParentScope(globalScope)
				compileScope.put("coffeeScriptSrc", compileScope, input)
				def result = cx.evaluateString(compileScope, "CoffeeScript.compile(coffeeScriptSrc)", "CoffeeScript compile command", 0, null)
				return result
			} catch(Exception e) {
				throw new Exception("""
				CoffeeScript Engine compilation of coffeescript to javascript failed.
				$e
				""")
			} finally {
				Context.exit()
			}
		}
	}

	/**
	 * Processes an input string of coffeescript using node.js (Don't use directly)
	* @param   input String input coffee script text to be converted to javascript
	* @param   AssetFile instance of the asset file from which this file came from. Not actually used currently for this implementation.
	* @return  String of compiled javascript
	 */
	def processWithNode(input, assetFile) {
		def nodeProcess
		def output = new StringBuilder()
		def err = new StringBuilder()

		try {
			def command = "${ isWindows() ? 'cmd /c ' : '' }coffee -csp"
			nodeProcess = command.execute()
			nodeProcess.getOutputStream().write(input.bytes)
			nodeProcess.getOutputStream().flush()
			nodeProcess.getOutputStream().close()
			nodeProcess.waitForProcessOutput(output, err)
			if(err) {
				throw new Exception(err.toString())
			}
			return output.toString()
		} catch(Exception e) {
			throw new Exception("""
			Node.js CoffeeScript Engine compilation of coffeescript to javascript failed.
			$e
			""")
		}
	}

	/**
	 * Determins if this is on a windows platform or not (used for node system path)
	 * @return Boolean true if this is a windows machine
	 */
	Boolean isWindows() {
		String osName = System.getProperty("os.name");
		return (osName != null && osName.contains("Windows"))
	}


	/**
	 * Determins if NODE is supported on the System path
	 * @return Boolean true if NODE.js is supported on the system path
	 */
	Boolean isNodeSupported() {
		if(NODE_SUPPORTED == null) {
			def nodeProcess
			def output = new StringBuilder()
			def err = new StringBuilder()

			try {
				def command = "${ isWindows() ? 'cmd /c ' : '' }coffee -v"
				nodeProcess = command.execute()
				nodeProcess.waitForProcessOutput(output, err)
				if(err) {
					NODE_SUPPORTED = false
				}
				else {
					NODE_SUPPORTED = true
				}
			} catch(Exception e) {
				NODE_SUPPORTED = false
			}
		}
			return NODE_SUPPORTED
	}

}

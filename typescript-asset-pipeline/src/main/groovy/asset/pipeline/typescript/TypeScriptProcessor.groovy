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

import asset.pipeline.AssetFile
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler

// TypeScript engine will attempt to use Node.JS tsc  if it is available on
// the system path. If not, it uses Mozilla Rhino to compile the TypeScript
// template using the javascript in-browser compiler.
class TypeScriptProcessor extends AbstractProcessor {

	static Boolean NODE_SUPPORTED
	Scriptable globalScope
	ClassLoader classLoader

	static org.mozilla.javascript.Script compilerScript
	static org.mozilla.javascript.Script processScript
	private static final $LOCK = new Object[0]
	TypeScriptProcessor(AssetCompiler precompiler) {
		super(precompiler)
		if(!isNodeSupported()) {
			try {
				classLoader = getClass().getClassLoader()
				Context cx = Context.enter()
				cx.setOptimizationLevel(-1)
				globalScope = cx.initStandardObjects()
				loadTypeScript(cx)
			} catch(Exception e) {
				throw new Exception("TypeScript Engine initialization failed.", e)
			} finally {
				try {
					Context.exit()
				} catch(IllegalStateException e) {
				}
			}
		}
	}

	protected void loadTypeScript(Context cx) {
		if(!compilerScript) {
			synchronized($LOCK) {
				if(!compilerScript) {
					def typeScriptJsResource = classLoader.getResource('asset/pipeline/typescript/tsServices.js')
					compilerScript = cx.compileString(typeScriptJsResource.getText('UTF-8'),typeScriptJsResource.file,1,null)
				}
			}
		}
		compilerScript.exec(cx,globalScope)

		if(!processScript) {
			synchronized($LOCK) {
				if(!processScript) {
					processScript = cx.compileString("ts.transpile(typeScriptSrc)", "TypeScript compile command", 0, null)
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
				compileScope.put("typeScriptSrc", compileScope, input)
				def result = processScript.exec(cx,compileScope)
				return result
			} catch(Exception e) {
				throw new Exception("""
				TypeScript Engine compilation of coffeescript to javascript failed.
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
			Node.js TypeScript Engine compilation of coffeescript to javascript failed.
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
		return false;
		if(NODE_SUPPORTED == null) {
			def nodeProcess
			def output = new StringBuilder()
			def err = new StringBuilder()

			try {
				def command = "${ isWindows() ? 'cmd /c ' : '' }tsc --version"
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

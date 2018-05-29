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


import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetHelper
import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.CacheManager
import asset.pipeline.GenericAssetFile
import asset.pipeline.JsAssetFile
import sun.net.www.content.text.Generic

import java.util.regex.Pattern
import com.google.javascript.jscomp.*
import com.google.javascript.jscomp.CompilerOptions.LanguageMode

/**
 * This Processor converts ECMAScript 6 syntax to ES5
 *
 * @author David Estes
 */
class Es6Processor extends AbstractProcessor {
	ClassLoader classLoader

	private static final Pattern URL_CALL_PATTERN = ~/goog\.require\((?:\s*)(['"]?)([a-zA-Z0-9\-_.:\/@#?$ &+%=]++)\1?(?:\s*)\)/
	public static ThreadLocal<ArrayList> es6JsModules = new ThreadLocal<ArrayList>()
	public static ThreadLocal<String> baseModule = new ThreadLocal<String>()

	Es6Processor(final AssetCompiler precompiler) {
		super(precompiler)
		classLoader = getClass().getClassLoader()
	}


	String process(final String inputText, final AssetFile assetFile) {
		println "Checking Config: ${AssetPipelineConfigHolder.config}"
		if(!inputText) {
			return inputText
		}
		if(assetFile instanceof JsAssetFile) {
			if(!AssetPipelineConfigHolder.config?.enableES6 && ! AssetPipelineConfigHolder.config?."enable-es6") {
				return inputText
			}
		}
		def compiler = new Compiler()
		CompilerOptions options = new CompilerOptions()
		options.trustedStrings = true
		CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
		options.setLanguageIn(LanguageMode.ECMASCRIPT8)
		options.setLanguageOut(LanguageMode.ECMASCRIPT5)
		options.prettyPrint = true
		options.lineBreak = true
		options.preserveTypeAnnotations = true

		WarningLevel.QUIET.setOptionsForWarningLevel(options);
		String moduleName = AssetHelper.nameWithoutExtension(assetFile.path ?: 'unnamed.js')
		SourceFile sourceFile = SourceFile.fromCode(moduleName, inputText)


		def result = compiler.compile(CommandLineRunner.getDefaultExterns(),[sourceFile] as List<SourceFile>,options)
		def output = compiler.toSource()
		if(!output) {
			return inputText
		}

		final Map<String, String> cachedPaths = [:]
		Boolean originator = false
		if(!baseModule.get()) {
			baseModule.set(assetFile.path)
			originator = true
			es6JsModules.set([])
		}
		try {
			output = output.replaceAll(URL_CALL_PATTERN) { final String urlCall, final String quote, final String assetPath ->
				final Boolean cacheFound = cachedPaths.containsKey(assetPath)
				final String cachedPath = cachedPaths[assetPath]
				String modulePath = assetPath
				String subModuleName = assetPath
				if(assetPath.startsWith('module$')) {
					modulePath = assetPath.substring(7).replace('$','/').replace('_','-')
				}
				String replacementPath
				if (cacheFound) {
					if(cachedPath == null) {
						return "goog.require(${quote}${assetPath}${quote})"
					} else {
						return "goog.require(${quote}${cachedPath}${quote})"
					}
				} else if(assetPath.size() > 0) {
					println "looking for path: ${assetPath}"
					AssetFile currFile
					if(!assetPath.startsWith('/')) {
						def relativeFileName = [ assetFile.parentPath, modulePath ].join( AssetHelper.DIRECTIVE_FILE_SEPARATOR )
						currFile = AssetHelper.fileForUri(relativeFileName,'application/javascript')
					}
					if(!currFile) {
						currFile = AssetHelper.fileForUri(modulePath,'application/javascript')
					}
					if(!currFile) {
						currFile = AssetHelper.fileForUri(modulePath + '/' + modulePath,'application/javascript')
					}

					//Time to look for index.js
					if(!currFile) {
						println "module not found, checking for index.js"
						if(!assetPath.startsWith('/')) {
							def relativeFileName = [ assetFile.parentPath, modulePath, 'index.js' ].join( AssetHelper.DIRECTIVE_FILE_SEPARATOR )
							currFile = AssetHelper.fileForUri(relativeFileName + '/index.js','application/javascript')
						}
						if(!currFile) {
							currFile = AssetHelper.fileForUri(modulePath + '/index.js','application/javascript')
						}
						if(!currFile) {
							currFile = AssetHelper.fileForUri(modulePath + '/' + modulePath + '/index.js','application/javascript')
						}
						println "Checking for currFile: ${currFile}"
					}

					if(!currFile) {
						if(!assetPath.startsWith('/')) {
							def relativeFileName = [ assetFile.parentPath, modulePath ].join( AssetHelper.DIRECTIVE_FILE_SEPARATOR )
							currFile = AssetHelper.fileForUri(relativeFileName)
						}
						if(!currFile) {
							currFile = AssetHelper.fileForUri(modulePath)
						}
						if(!currFile) {
							currFile = AssetHelper.fileForUri(modulePath + '/' + modulePath)
						}
						if(currFile instanceof GenericAssetFile) {
							//We have an image file we need its url or byte stream
						} else {
							currFile = null
						}
					}
					if(!currFile) {
						//Time to check FileLoader and see if this file exists as anything else

						cachedPaths[assetPath] = null
						return "goog.require(${quote}${assetPath}${quote})"
					} else {
						currFile.baseFile = assetFile.baseFile ?: assetFile
						appendModule(currFile, subModuleName, assetFile)
						String path = AssetHelper.fileNameWithoutExtensionFromArtefact(currFile.path, currFile)
						cachedPaths[assetPath] = assetPath
						return "goog.require(${quote}${assetPath}${quote})"
					}
				} else {
					return "require(${quote}${assetPath}${quote})"
				}
			}

			if(baseModule.get() == assetFile.path && es6JsModules.get()) {
				def googBaseJsResource = classLoader.getResource('asset/pipeline/goog/base.js')
				output = googBaseJsResource.text + "\n" + es6Runtime() + modulesJs() + output
			}
			else if(baseModule.get() == assetFile.path) {
				def googBaseJsResource = classLoader.getResource('asset/pipeline/goog/base.js')
				output = googBaseJsResource.text + "\n" + es6Runtime() + output
			}	
			
		} finally {
			if(originator) {
				es6JsModules.set([])
				baseModule.set(null)
			}
		}

		AssetFile baseFile = assetFile.baseFile ?: assetFile
		if(!baseFile.matchedDirectives.find{it == '__goog_base'}) {
			
		}
		return output
	}

	private appendModule(AssetFile assetFile, String moduleName, AssetFile currentFile) {
		List moduleList = es6JsModules.get()
		if(!moduleList) {
			moduleList = []
			es6JsModules.set(moduleList)
		}

		if(moduleList.find{Map<String,String> row -> row.name == assetFile.path}) {
			return
		}
		Map<String,String> moduleMap = [:] as Map<String,String>
		moduleMap.name = assetFile.path
		moduleMap.value = ' \n'
		def insertPosition = null
		moduleList.eachWithIndex{ row, index ->
			if(row.name == currentFile.path) {
				insertPosition = index
			}
		}
		if(insertPosition != null) {
			moduleList.add(insertPosition,moduleMap)	
		} else {
			moduleList << moduleMap	
		}
		moduleMap.value = encapsulateModule(assetFile, moduleName)
		CacheManager.addCacheDependency(baseModule.get(), assetFile)
	}

	private String modulesJs() {
		String output = ''

		output += es6JsModules.get()?.collect { Map<String,String> row ->
			"${row.value};"
		}.join('\n')

		return output
	}


	private encapsulateModule(AssetFile assetFile, moduleName) {
		String output = assetFile.processedStream(precompiler,true)
		if(!output.contains(moduleName)) {
			//its not an ES6 module we need to wrap it in a require js module syntax
					String encapsulation = """
goog.provide('${moduleName}');
(function() {
  var module = {exports: {}};
  var exports = module.exports;

  ${output}

  ${moduleName} = {default:module['exports']};
  return module;
})()
"""
			return encapsulation
		} else {
			return output
		}
	}



	private String es6Runtime() {
		def es6RuntimeJsResource = classLoader.getResource('asset/pipeline/goog/es6_runtime.js')
		return es6RuntimeJsResource.text + "\n" + "var _asset_pipeline_loaded_modules = _asset_pipeline_loaded_modules || {};\n"
	}
}

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
import asset.pipeline.JsAssetFile

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
	public static ThreadLocal<Map<String,String>> es6JsModules = new ThreadLocal<Map<String,String>>()
	public static ThreadLocal<String> baseModule = new ThreadLocal<String>()

	Es6Processor(final AssetCompiler precompiler) {
		super(precompiler)
		classLoader = getClass().getClassLoader()
	}


	String process(final String inputText, final AssetFile assetFile) {

		if(!inputText) {
			return inputText
		}
		if(assetFile instanceof JsAssetFile) {
			if(!AssetPipelineConfigHolder.config?.enableES6) {
				return inputText
			}
		}
		def compiler = new Compiler()
		CompilerOptions options = new CompilerOptions()
		options.trustedStrings = true
		CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
		options.setLanguageIn(LanguageMode.ECMASCRIPT6)
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
			es6JsModules.set([:] as Map<String,String>)
		}
		try {
			output = output.replaceAll(URL_CALL_PATTERN) { final String urlCall, final String quote, final String assetPath ->
				final Boolean cacheFound = cachedPaths.containsKey(assetPath)
				final String cachedPath = cachedPaths[assetPath]
				String modulePath = assetPath
				if(assetPath.startsWith('module$')) {
					modulePath = assetPath.substring(7).replace('$','/')
				}
				String replacementPath
				if (cacheFound) {
					if(cachedPath == null) {
						return "goog.require(${quote}${assetPath}${quote})"
					} else {
						return "goog.require(${quote}${cachedPath}${quote})"
					}
				} else if(assetPath.size() > 0) {
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
					if(!currFile) {
						cachedPaths[assetPath] = null
						return "goog.require(${quote}${assetPath}${quote})"
					} else {
						currFile.baseFile = assetFile.baseFile ?: assetFile
						appendModule(currFile)
						String path = AssetHelper.nameWithoutExtension(currFile.path)
						cachedPaths[assetPath] = assetPath
						return "goog.require(${quote}${assetPath}${quote})"
					}
				} else {
					return "require(${quote}${assetPath}${quote})"
				}
			}


			if(baseModule.get() == assetFile.path && es6JsModules.get()) {
				output = modulesJs() + output
			}
		} finally {
			if(originator) {
				es6JsModules.set([:] as Map<String,String>)
				baseModule.set(null)
			}
		}

		AssetFile baseFile = assetFile.baseFile ?: assetFile
		if(!baseFile.matchedDirectives.find{it == '__goog_base'}) {
			def googBaseJsResource = classLoader.getResource('asset/pipeline/goog/base.js')
			output = googBaseJsResource.text + "\n" + output
			baseFile.matchedDirectives << '__goog_base'
		}
		return output
	}

	private appendModule(AssetFile assetFile) {
		Map<String,String> moduleMap = es6JsModules.get()
		if(!moduleMap) {
			moduleMap = [:] as Map<String,String>
			es6JsModules.set(moduleMap)
		}

		if(moduleMap[assetFile.path]) {
			return
		}
		moduleMap[assetFile.path] = ' \n'
		moduleMap[assetFile.path] = assetFile.processedStream(precompiler,true)
		CacheManager.addCacheDependency(baseModule.get(), assetFile)
	}

	private String modulesJs() {
		String output = ''
		output += es6JsModules.get()?.collect { path, encapsulation ->
			"${encapsulation};"
		}.join('\n')

		return output
	}
}

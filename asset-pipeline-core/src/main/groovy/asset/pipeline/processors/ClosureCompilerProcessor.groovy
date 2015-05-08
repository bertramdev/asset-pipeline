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

import asset.pipeline.AssetCompiler
import com.google.javascript.jscomp.*
import com.google.javascript.jscomp.CompilerOptions.LanguageMode
import java.util.logging.Level
import groovy.transform.CompileStatic

@CompileStatic
class ClosureCompilerProcessor {
	static contentTypes = ['application/javascript']

	AssetCompiler assetCompiler
	ClosureCompilerProcessor(AssetCompiler compiler) {
		this.assetCompiler = compiler
		com.google.javascript.jscomp.Compiler.setLoggingLevel(Level.INFO);
	}


	public String process(String fileName, String inputText, Map minifyOptions = [:]) {
		def compiler = new Compiler()
		CompilerOptions options = new CompilerOptions()
		options.trustedStrings = true

		translateMinifyOptions(options,minifyOptions)
		if(assetCompiler.options.enableSourceMaps) {
			setSourceMapOptions(options,minifyOptions, fileName)
		}

		WarningLevel.QUIET.setOptionsForWarningLevel(options);
		SourceFile sourceFile = SourceFile.fromCode(fileName + ".unminified.js", inputText)
		// def sourceFile = new SourceFile.Preloaded(fileName + ".unminified.js",fileName, inputText)
		// sourceFile.setCode(inputText)
		def result = compiler.compile([] as List<SourceFile>,[sourceFile] as List<SourceFile>,options)
		def output = compiler.toSource()
		if(compiler.sourceMap) {
			File mapFile = new File(assetCompiler.options.compileDir as String,fileName + ".js.map")

			if(!mapFile.exists()) {
				mapFile.parentFile.mkdirs()
			}
			File unminifiedFile = new File(assetCompiler.options.compileDir as String,fileName + ".unminified.js")
			unminifiedFile.text = inputText
			mapFile.createNewFile()
			FileWriter outputWriter = new FileWriter(mapFile)
			compiler.sourceMap.setWrapperPrefix("//# sourceMappingURL=${fileName + '.js.map'}\n")
			compiler.sourceMap.appendTo(outputWriter,fileName + ".js")
			outputWriter.close();
			output = "//# sourceMappingURL=${fileName + '.js.map'}\n" + output
		}
		return output
	}

	public void translateMinifyOptions(CompilerOptions compilerOptions, Map minifyOptions) {
		def defaultOptions = [
			languageMode: 'ES5',
			optimizationLevel: 'SIMPLE' //WHITESPACE , ADVANCED
		]
		minifyOptions = defaultOptions + minifyOptions
		LanguageMode languageIn = evaluateLanguageMode(minifyOptions.get('languageMode') as String)
		if(minifyOptions.targetLanguage) {
			LanguageMode languageOut = evaluateLanguageMode(minifyOptions.get('targetLanguage') as String)
			compilerOptions.setLanguageIn(languageIn)
			compilerOptions.setLanguageOut(languageOut)
		} else {
			compilerOptions.setLanguage(languageIn)
		}
		setCompilationLevelOptions(compilerOptions, minifyOptions.get('optimizationLevel') as String)
	}


	private LanguageMode evaluateLanguageMode(String mode) {
		switch(mode?.toUpperCase()) {
			case 'ES6':
				return LanguageMode.ECMASCRIPT6
			case 'ES6_STRICT':
				return LanguageMode.ECMASCRIPT6_STRICT
			case 'ES5_SCRIPT':
				return LanguageMode.ECMASCRIPT5_STRICT
			case 'ES3':
				return LanguageMode.ECMASCRIPT3
			case 'ES5':
			default:
				return LanguageMode.ECMASCRIPT5
		}
	}

	private void setCompilationLevelOptions(CompilerOptions options ,String mode) {
		switch(mode?.toUpperCase()) {
			case 'WHITESPACE':
			case 'WHITESPACE_ONLY':
				CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);
				break;
			case 'ADVANCED':
				CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
				break;
			case 'SIMPLE':
			default:
				CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
		}
	}

	private void setSourceMapOptions(CompilerOptions compilerOptions, Map minifyOptions, String filename) {
		compilerOptions.sourceMapDetailLevel = SourceMap.DetailLevel.ALL;
		compilerOptions.sourceMapFormat = SourceMap.Format.DEFAULT;
		//compilerOptions.sourceMapLocationMappings =
		compilerOptions.sourceMapOutputPath = "${filename}.js.map"
	}



}

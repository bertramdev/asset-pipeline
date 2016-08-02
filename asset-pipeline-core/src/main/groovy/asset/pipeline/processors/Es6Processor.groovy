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
import asset.pipeline.JsAssetFile

import java.util.regex.Pattern
import com.google.javascript.jscomp.*
import com.google.javascript.jscomp.CompilerOptions.LanguageMode

/**
 * This Processor converts EcmaScript 6 syntax to ES5
 *
 * @author David Estes
 */
class Es6Processor extends AbstractProcessor {



	Es6Processor(final AssetCompiler precompiler) {
		super(precompiler)
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
		SourceFile sourceFile = SourceFile.fromCode(assetFile.name ?: 'unnamed.js', inputText)

		def result = compiler.compile(CommandLineRunner.getDefaultExterns(),[sourceFile] as List<SourceFile>,options)
		def output = compiler.toSource()
		if(!output) {
			return inputText
		}
		return output
	}
}

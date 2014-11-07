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
import com.google.javascript.jscomp.*
import com.google.javascript.jscomp.CompilerOptions.LanguageMode
import java.util.logging.Level

class ClosureCompilerProcessor {
	static contentTypes = ['application/javascript']


	ClosureCompilerProcessor() {


		com.google.javascript.jscomp.Compiler.setLoggingLevel(Level.INFO);
	}



	def process(fileName, inputText, myoptions = [:]) {
		def compiler = new Compiler()
		CompilerOptions options = new CompilerOptions();
		options.languageIn = LanguageMode.ECMASCRIPT5
		CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
		WarningLevel.QUIET.setOptionsForWarningLevel(options);

		def sourceFile = new SourceFile(fileName)
		sourceFile.setCode(inputText)
		def result = compiler.compile([] as List<SourceFile>,[sourceFile] as List<SourceFile>,options)
		def output = compiler.toSource()
		return output
	}




}

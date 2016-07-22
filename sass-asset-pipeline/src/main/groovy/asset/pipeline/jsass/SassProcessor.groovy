/*
 * Copyright 2016 the original author or authors.
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
package asset.pipeline.jsass

import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetPipelineConfigHolder
import groovy.util.logging.Commons
import io.bit3.jsass.Compiler
import io.bit3.jsass.Options
import io.bit3.jsass.OutputStyle

@Commons
class SassProcessor extends AbstractProcessor {
    final Compiler compiler = new Compiler();
    final Options options = new Options();

    SassProcessor(AssetCompiler precompiler) {
        super(precompiler)
        // TODO: Add support for more configuration options (like source maps)
        options.setOutputStyle(AssetPipelineConfigHolder.config?.sass?.outputStyle ?: OutputStyle.EXPANDED)
        options.setSourceComments(AssetPipelineConfigHolder.config?.sass?.sourceComments ?: true)
     }

    /**
     * Called whenever the asset pipeline detects a change in the file provided as argument
     * @param input the content of the SCSS file to compile
     * @param assetFile
     * @return
     */
    String process(String input, AssetFile assetFile) {
        options.getImporters().add(new SassAssetFileImporter(assetFile))
        
        log.debug "Compiling $assetFile.name"
        if(assetFile.name.endsWith('.sass')) {
            options.setIsIndentedSyntaxSrc(true); 
        } else {
            options.setIsIndentedSyntaxSrc(false); 
        }
        def output = compiler.compileString(input, assetFile.path.toURI(), null, options)
        return output.css
    }
}

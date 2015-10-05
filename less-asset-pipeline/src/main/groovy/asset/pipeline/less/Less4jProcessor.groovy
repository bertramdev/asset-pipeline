package asset.pipeline.less

import asset.pipeline.AssetHelper
import com.github.sommeri.less4j.LessCompiler
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler
import com.github.sommeri.less4j_javascript.Less4jJavascript
import groovy.util.logging.Log4j
import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import com.github.sommeri.less4j.Less4jException

@Log4j
class Less4jProcessor extends AbstractProcessor {

    Less4jProcessor(precompiler) {
        super(precompiler)
    }

    String process(String input, AssetFile assetFile) {
        try {
            def lessSource = new AssetPipelineLessSource(assetFile, input, [baseFile: assetFile, precompiler: this.precompiler])

            LessCompiler.Configuration configuration = new LessCompiler.Configuration()
            Less4jJavascript.configure(configuration);
            LessCompiler compiler = new ThreadUnsafeLessCompiler();
            def compilationResult = compiler.compile(lessSource, configuration);

            def result = compilationResult.getCss()

            return result
        } catch (Less4jException l4e) {
            def errorDetails = "LESS Engine Compiler Failed - ${assetFile.name}:\n --${l4e.message}\n"
            if (precompiler) {
                errorDetails += "**Did you mean to compile this file individually (check docs on exclusion)?**\n"
            }
            throw new Exception(errorDetails, l4e)

        } catch (Exception e) {
            def errorDetails = "LESS Engine Compiler Failed - ${assetFile.name}.\n"
            if (precompiler) {
                errorDetails += "**Did you mean to compile this file individually (check docs on exclusion)?**\n"
            }
           // log.error(errorDetails)
            throw new Exception(errorDetails, e)

        }
    }

}

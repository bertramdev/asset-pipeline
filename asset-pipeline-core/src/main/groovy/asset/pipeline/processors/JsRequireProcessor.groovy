package asset.pipeline.processors

import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import groovy.transform.CompileStatic

@CompileStatic
public class JsRequireProcessor extends AbstractProcessor {
	JsRequireProcessor(final AssetCompiler precompiler) {
		super(precompiler)
	}


	String process(final String inputText, final AssetFile assetFile) {

		return inputText
	}
}
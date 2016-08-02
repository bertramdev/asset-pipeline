package asset.pipeline.processors

import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import groovy.transform.CompileStatic

import java.util.regex.Pattern

@CompileStatic
class JsRequireProcessor extends AbstractUrlRewritingProcessor {

	private static final Pattern URL_CALL_PATTERN = ~/require\((?:\s*)(['"]?)([a-zA-Z0-9\-_.:\/@#? &+%=]++)\1?(?:\s*)\)/
	public static ThreadLocal<Map<String,String>> commonJsModules = new ThreadLocal<Map<String,String>>()
	public static ThreadLocal<String> baseModule = new ThreadLocal<String>()

	static {
        doNotInsertCacheDigestIntoUrlForCompiledExtension('html')
    }


	JsRequireProcessor(final AssetCompiler precompiler) {
		super(precompiler)
	}


	String process(final String inputText, final AssetFile assetFile) {
		final Map<String, String> cachedPaths = [:]
		Boolean originator = false
		if(!baseModule.get()) {
			baseModule.set(assetFile.path)
			originator = true
			commonJsModules.set([:] as Map<String,String>)
		}
		try {
			String result =	inputText.replaceAll(URL_CALL_PATTERN) { final String urlCall, final String quote, final String assetPath ->
				final String cachedPath = cachedPaths[assetPath]

				String replacementPath
				if (cachedPath != null) {
					return "_asset_pipeline_require(${quote}${cachedPath}${quote})"
				} else if(assetPath.size() > 0) {
					final AssetFile currFile = AssetHelper.fileForUri(assetPath,'application/javascript')
					if(!currFile) {
						currFile = AssetHelper.fileForUri(assetPath + '/' + assetPath,'application/javascript')
					}
					if(!currFile) {
						cachedPaths[assetPath] = assetPath
						return "_asset_pipeline_require(${quote}${assetPath}${quote})"
					} else {
						currFile.baseFile = assetFile.baseFile ?: assetFile
						appendModule(currFile)
						cachedPaths[assetPath] = currFile.path
						return "_asset_pipeline_require(${quote}${currFile.path}${quote})"
					}
				} else {
					return "require(${quote}${assetPath}${quote})"
				}
			}


			if(baseModule.get() == assetFile.path && commonJsModules.get()) {
				result = requireMethod + modulesJs() + result
			}
			return result
		} finally {
			if(originator) {
				commonJsModules.set([:] as Map<String,String>)
				baseModule.set(null)
			}
		}
	}

	private appendModule(AssetFile assetFile) {
		Map<String,String> moduleMap = commonJsModules.get()
		if(!moduleMap) {
			moduleMap = [:] as Map<String,String>
			commonJsModules.set(moduleMap)
		}

		if(moduleMap[assetFile.path]) {
			return
		}
		moduleMap[assetFile.path] = encapsulateModule(assetFile)
	}

	private encapsulateModule(AssetFile assetFile) {
		String encapsulation = """
(function() {
  var module = {exports: {}};
  var exports = module.exports;

  ${assetFile.processedStream(precompiler)}

  return module;
})
"""

		return encapsulation
	}


	private String modulesJs() {
		String output = "var _asset_pipeline_modules = _asset_pipeline_modules || {};\n"
		output += commonJsModules.get()?.collect { path, encapsulation ->
			"_asset_pipeline_modules['${path}'] = ${encapsulation};"
		}.join('\n')

		return output
	}



	static final String requireMethod = """
var _asset_pipeline_require = function(path) {
	var module = _asset_pipeline_modules[path];
	if(module != undefined) {
		return module().exports;
	}
	return null;
};

"""
}

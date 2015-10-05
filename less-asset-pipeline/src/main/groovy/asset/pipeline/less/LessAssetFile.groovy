package asset.pipeline.less

import asset.pipeline.AbstractAssetFile
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetHelper
import asset.pipeline.CacheManager
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.processors.CssProcessor
import java.util.regex.Pattern

class LessAssetFile extends AbstractAssetFile {
    static final String contentType = 'text/css'
    static extensions = ['less', 'css.less']
    static final String compiledExtension = 'css'
    static processors = [CssProcessor]
    Pattern directivePattern = ~/(?m)\*=(.*)/

    String processedStream(AssetCompiler precompiler) {
        def fileText
		def skipCache = precompiler ?: (!processors || processors.size() == 0)
		if(baseFile?.encoding || encoding) {
			fileText = inputStream?.getText(baseFile?.encoding ? baseFile.encoding : encoding)
		} else {
			fileText = inputStream?.text
		}

		def md5 = AssetHelper.getByteDigest(fileText.bytes)
		if(!skipCache) {
			def cache = CacheManager.findCache(path, md5, baseFile?.path)
			if(cache) {
				return cache
			}
		}

        def lessProcessor
        def compilerMode = AssetPipelineConfigHolder.config?.less?.compiler ?: 'less4j'
        if (compilerMode != 'standard') {
            lessProcessor = new Less4jProcessor(precompiler)
        } else {
            lessProcessor = new LessProcessor(precompiler)
        }
        fileText = lessProcessor.process(fileText, this)

		for(processor in processors) {
			def processInstance = processor.newInstance(precompiler)
			fileText = processInstance.process(fileText, this)
		}

		if(!skipCache) {
			CacheManager.createCache(path, md5, fileText, baseFile?.path)
		}

		return fileText
    }

}

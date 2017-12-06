package asset.pipeline.groocss

import asset.pipeline.AbstractAssetFile
import asset.pipeline.AssetCompiler
import asset.pipeline.CacheManager
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.processors.CssProcessor
import org.groocss.Config
import org.groocss.GrooCSS

import java.util.regex.Pattern

class GroocssAssetFile extends AbstractAssetFile {

    static final String contentType = 'text/css'
    static extensions = ['groocss', 'css.groovy']
    static final String compiledExtension = 'css'
    static processors = [CssProcessor]
    Pattern directivePattern = ~/(?m)\*=(.*)/

    String processedStream(AssetCompiler precompiler, Boolean skipCaching = false) {
        def fileText
        def skipCache = precompiler ?: (!processors || processors.size() == 0)
        InputStream sourceStream = getInputStream()
        try {
            if(baseFile?.encoding || encoding) {
                fileText = sourceStream?.getText(baseFile?.encoding ? baseFile.encoding : encoding)
            } else {
                fileText = sourceStream?.getText("UTF-8")
            }

            String md5 = null
            if(!skipCache) {
                md5 = getByteDigest()
                def cache = CacheManager.findCache(path, md5, baseFile?.path)
                if(cache) {
                    return cache
                }
            }
            
            def conf = AssetPipelineConfigHolder.config
            def prettyPrint = conf?.groocss?.prettyPrint ?: false
            def compress = conf?.groocss?.compress ?: false
            def noExts = conf?.groocss?.noExts ?: false
            def convertUnderline = conf?.groocss?.convertUnderline ?: false
            def config = new Config(compress: compress, prettyPrint: prettyPrint,
                        convertUnderline: convertUnderline)

            if (noExts) config.noExts()

            fileText = GrooCSS.process(config, fileText)

            if (precompiler) {
                for(processor in processors) {
                    def processInstance = processor.newInstance(precompiler)
                    fileText = processInstance.process(fileText, this)
                }
            }

            if(!skipCache) {
                CacheManager.createCache(path, md5, fileText, baseFile?.path)
            }
        } finally {
            try { sourceStream?.close() } catch(Exception ex) {/*doesnt matter just tries to ensure close */}
        }

        return fileText
    }

}

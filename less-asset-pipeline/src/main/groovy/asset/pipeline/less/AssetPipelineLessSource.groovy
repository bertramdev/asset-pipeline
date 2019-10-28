package asset.pipeline.less

import com.github.sommeri.less4j.*
import groovy.util.logging.Slf4j

import asset.pipeline.CacheManager
import asset.pipeline.AssetHelper
import asset.pipeline.AssetCompiler
import asset.pipeline.processors.CssProcessor

@Slf4j
class AssetPipelineLessSource extends LessSource {
	def sourceFile
	String contents
	AssetCompiler precompiler
	Map options
	public AssetPipelineLessSource(file, contents, options=[:]) {
		sourceFile = file
		this.options = options
		this.contents = contents
		this.precompiler = options.precompiler
	}

 	public LessSource relativeSource(String fileName) {
			def newFile
 		    if( fileName.startsWith( AssetHelper.DIRECTIVE_FILE_SEPARATOR ) ) {
                newFile = AssetHelper.fileForUri( fileName, 'text/css', null, options.baseFile )
            }
            else {
                def relativeFileName = [ sourceFile.parentPath, fileName ].join( AssetHelper.DIRECTIVE_FILE_SEPARATOR )
                newFile = AssetHelper.fileForUri( relativeFileName, 'text/css', null, options.baseFile )
            }


            if( !newFile && !fileName.startsWith( AssetHelper.DIRECTIVE_FILE_SEPARATOR ) ) {
				newFile = AssetHelper.fileForUri( AssetHelper.DIRECTIVE_FILE_SEPARATOR + fileName, 'text/css', null, options.baseFile )
            }
            else if (!newFile) {
                log.warn( "Unable to Locate Asset: ${ fileName }" )
            }

			if(newFile) {
				CacheManager.addCacheDependency(options.baseFile?.path ?: sourceFile.path, newFile)

				return new AssetPipelineLessSource(newFile, null, options)
			}


		    return null
 	}


	public String getContent() {
		if(contents) {
			return contents
		}
		def cssProcessor = new CssProcessor(this.precompiler)
		return cssProcessor.process(sourceFile.inputStream.text, sourceFile)
	}

	public byte[] getBytes() {
		if(contents) {
			return contents.bytes
		}
		return sourceFile.inputStream.bytes
	}
}

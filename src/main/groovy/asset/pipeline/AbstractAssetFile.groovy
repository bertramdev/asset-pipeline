package asset.pipeline

import java.io.InputStream
import asset.pipeline.fs.AssetResolverInterface

abstract class AbstractAssetFile implements AssetFile {
	String path
	AssetFile baseFile
	AssetResolverInterface sourceResolver
	String encoding

	private def _byteCache

	Closure inputStreamSource = {} //Implemented by AssetResolver

	InputStream getInputStream() {
		return inputStreamSource()
	}

	public String getParentPath() {
		def pathArgs = path.split("/")
		if(pathArgs.size() == 1) {
			return null
		}
		return pathArgs[0..(pathArgs.size()-2)].join("/")
	}

	public String getName() {
		path.split("/")[-1]
	}


	String processedStream(precompiler) {
		def fileText
		def skipCache = precompiler ?: (!processors || processors.size() == 0)
		def cacheKey
		if(baseFile?.encoding || encoding) {
			fileText = inputStream?.getText(baseFile?.encoding ? baseFile.encoding : encoding)
		} else {
			fileText = inputStream?.text
		}

		def md5 = AssetHelper.getByteDigest(fileText.bytes)
		if(!skipCache) {
			def cache = CacheManager.findCache(canonicalPath, md5, baseFile?.path)
			if(cache) {
				return cache
			}
		}
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

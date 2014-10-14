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

package asset.pipeline

import java.io.InputStream
import asset.pipeline.fs.AssetResolverInterface

/**
* This is the base Asset File specification class. An AssetFile object should extend this abstract base class.
* The AssetFile specification provides information on what processors need to be run on a file.
* A file is matched to an AssetFile specification based on its content type and extension.
* If a file is not matched to a processable AssetFile entity, see the {@link asset.pipeline.GenericAssetFile}.
*
* @author David Estes
*/
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

	public String toString() {
		return path
	}
}

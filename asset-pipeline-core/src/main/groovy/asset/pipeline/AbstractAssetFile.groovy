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

import asset.pipeline.fs.AssetResolver
import groovy.transform.CompileStatic
/**
* This is the base Asset File specification class. An AssetFile object should extend this abstract base class.
* The AssetFile specification provides information on what processors need to be run on a file.
* A file is matched to an AssetFile specification based on its content type and extension.
* If a file is not matched to a processable AssetFile entity, see the {@link asset.pipeline.GenericAssetFile}.
*
* @author David Estes
*/
@CompileStatic
abstract class AbstractAssetFile implements AssetFile {
	String path
	AssetFile baseFile
	AssetResolver sourceResolver
	String encoding

	Closure inputStreamSource = {} //Implemented by AssetResolver
	byte[] byteCache


	// @CompileStatic
	InputStream getInputStream() {
		if(byteCache == null) {
			byteCache = (inputStreamSource() as InputStream).bytes
		}
		return new ByteArrayInputStream(byteCache)
	}

    String getCanonicalPath() {
        return path
    }

	public String getParentPath() {
		String[] pathArgs = path.split("/")
		if(pathArgs.size() == 1) {
			return null
		}
		return (Arrays.copyOfRange(pathArgs,0,pathArgs.size() - 1) as String[]).join("/")
	}

	public String getName() {
		path.split("/")[-1]
	}


	String processedStream(AssetCompiler precompiler) {
		String fileText
		Boolean skipCache = precompiler ?: (!processors || processors.size() == 0)
		String cacheKey
		if(baseFile?.encoding || encoding) {
			fileText = inputStream?.getText(baseFile?.encoding ? baseFile.encoding : encoding)
		} else {
			fileText = inputStream?.text
		}

		String md5 = null
		if(!skipCache) {
			md5 = AssetHelper.getByteDigest(fileText.bytes)
			String cache = CacheManager.findCache(path, md5, baseFile?.path)
			if(cache) {
				return cache
			}
		}
	    for(Class<Processor> processor in processors) {
			Processor processInstance = processor.newInstance(precompiler) as Processor
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

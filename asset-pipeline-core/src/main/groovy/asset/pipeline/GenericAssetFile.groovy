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

import groovy.transform.CompileStatic
import java.security.MessageDigest
import java.security.DigestInputStream

/**
 * Generic implementation of the {@link AssetFile} interface
 */
@CompileStatic
class GenericAssetFile extends AbstractAssetFile {

	String path

	Closure inputStreamSource = {} //Implemented by AssetResolver

	InputStream getInputStream() {
		this.digest = MessageDigest.getInstance("MD5")
		this.digestStream = new DigestInputStream((InputStream)inputStreamSource(),digest)
		return digestStream
	}

	public String getByteDigest() {
		if(!digestStream) {
			getInputStream()
		}

		byte[] buffer = new byte[1024]
		int nRead
		while ((nRead = digestStream.read(buffer, 0, buffer.length)) != -1) {
		  // noop (just to complete the stream)
		}

		return digest.digest().encodeHex().toString()
	}

	public String getParentPath() {
		List<String> pathArgs = path.tokenize("/")
		if(pathArgs.size() == 1) {
			return null
		}
		return (pathArgs[0..(pathArgs.size()-2)]).join("/")
	}

	public Byte[] getBytes() {
		return inputStream.bytes
	}

	public String getName() {
		path.split("/")[-1]
	}

}

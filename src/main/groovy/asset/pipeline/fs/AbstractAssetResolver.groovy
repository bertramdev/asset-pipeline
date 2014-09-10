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

package asset.pipeline.fs

import asset.pipeline.*

/**
* The abstract class for any helper methods in resolving files
* @author David Estes
*/
abstract class AbstractAssetResolver implements AssetResolverInterface {
	String name

	public def getAsset(String relativePath, String contentType = null, String extension = null) {

	}

	public def getAssets(String basePath, String contentType = null, String extension = null,  Boolean recursive = true) {

	}

}

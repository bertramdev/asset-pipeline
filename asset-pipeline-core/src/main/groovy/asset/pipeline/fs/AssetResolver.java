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

package asset.pipeline.fs;

import asset.pipeline.*;
import java.util.List;
import java.util.Collection;


/**
* A public interface defining a class that can resolve files
 *
* @author David Estes
*/
public interface AssetResolver {

	public String getName();
	/**
	* Resolves a file given a relative path to this particular resolver
	*/
	public AssetFile getAsset(String relativePath, String contentType, String extension, AssetFile baseFile);
	public AssetFile getAsset(String relativePath, String contentType);
	public AssetFile getAsset(String relativePath);

	/**
	* Resolves multiple files given a basePath and content type
	* @param basePath String containing the base prefix path
	* @param contentType String containing the contentType filter
	* @param extension   String containing the target extension
	* @return AssetFile[] Returns a list of Asset Files
	*/
	public List<AssetFile> getAssets(String basePath, String contentType, String extension,  Boolean recursive, AssetFile relativeFile, AssetFile baseFile);
	public List<AssetFile> getAssets(String basePath, String contentType, String extension);
	public List<AssetFile> getAssets(String basePath, String contentType);
	public List<AssetFile> getAssets(String basePath);

	public Collection<AssetFile> scanForFiles(List<String> excludePatterns, List<String> includePatterns);
}

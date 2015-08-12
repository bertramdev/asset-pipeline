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

import java.util.concurrent.ConcurrentHashMap;
import groovy.json.JsonSlurper;
import groovy.json.JsonOutput;

/**
 * Simple cache manager for Asset Pipeline
 *
 * @author David Estes
 * @author Graeme Rocher
 */
public class CacheManager {
	static final String CACHE_LOCATION = ".asset-cache"
	static final Integer CACHE_DEBOUNCE_MS = 5000 // Debounce 5 seconds
	static Map<String, Map<String, Object>> cache = new ConcurrentHashMap<String, Map<String, Object>>()
	static final Object LOCK_OBJECT = new Object()
	static CachePersister cachePersister

	public static String findCache(String fileName, String md5, String originalFileName = null) {
		def cacheRecord = cache[fileName]

		if(cacheRecord && cacheRecord.md5 == md5 && cacheRecord.originalFileName == originalFileName) {
			def cacheFiles = cacheRecord.dependencies.keySet()
			def expiredCacheFound = cacheFiles.find { String cacheFileName ->
				def cacheFile = AssetHelper.fileForUri(cacheFileName)
				if(!cacheFile)
					return true
				def depMd5 = AssetHelper.getByteDigest(cacheFile.inputStream.bytes)
				if(cacheRecord.dependencies[cacheFileName] != depMd5) {
					return true
				}
				return false
			}

			if(expiredCacheFound) {
				cache.remove(fileName)
				asyncCacheSave()
				return null
			}
			return cacheRecord.processedFileText
		} else if (cacheRecord) {
			cache.remove(fileName)
			asyncCacheSave()
			return null
		}
	}

	public static void createCache(String fileName, String md5Hash, String processedFileText, String originalFileName = null) {
        def thisCache = cache
        def cacheRecord = thisCache[fileName]
		if(cacheRecord) {
			thisCache[fileName] = cacheRecord + [
				md5: md5Hash,
				originalFileName: originalFileName,
				processedFileText: processedFileText
			]
		} else {
			thisCache[fileName] = [
				md5: md5Hash,
				originalFileName: originalFileName,
				processedFileText: processedFileText,
				dependencies: [:]
			]
		}
		asyncCacheSave()
	}

	public static void addCacheDependency(String fileName, AssetFile dependentFile) {
		def cacheRecord = cache[fileName]
		if(!cacheRecord) {
			createCache(fileName, null, null)
			cacheRecord = cache[fileName]
		}
		def newMd5 = AssetHelper.getByteDigest(dependentFile.inputStream.bytes)
		cacheRecord.dependencies[dependentFile.path] = newMd5
		asyncCacheSave()
	}


	public static void asyncCacheSave() {
		synchronized(LOCK_OBJECT) {
			if(!cachePersister) {
				cachePersister = new CachePersister()
				cachePersister.start()
			}	
		}
		cachePersister.debounceSave(CACHE_DEBOUNCE_MS);
	}


	public static void save() {
		String jsonCache = JsonOutput.toJson(cache)
		File assetFile = new File(CACHE_LOCATION)
		assetFile.text = jsonCache
	}

	/**
	* Loads an Asset Cache dependency graph for asset-pipeline.
	* This is currently encoded in JSON
	* TODO: Change this potentially to be a file per key for quicker access
	* If the asset cache file does not exist or a cache is already loaded, the cache store is not parsed.
	*/
	public static void loadPersistedCache() {
		if(cache) {
			return;
		}
		File assetFile = new File(CACHE_LOCATION)
		if(!file.exists()) {
			return;
		}
		try {
			def jsonCache = new JsonSlurper().parse(assetFile)
			jsonCache?.each{ entry ->
				cache[entry.key] = entry.value
			}
		} catch(ex) {
			// If there is a parser error from a previous bad cache flush ignore it and move on
		}
		
	}
}

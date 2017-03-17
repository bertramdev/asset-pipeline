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
import groovy.transform.CompileStatic
import groovy.util.logging.Commons

import java.util.jar.JarEntry
import java.util.regex.Pattern
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.io.BufferedInputStream

/**
 * Implementation of the {@link AssetResolver} interface for resolving from JAR files or ZIP files
 *
 * @author David Estes
 * @author Graeme Rocher
 */
@Commons
class JarAssetResolver extends AbstractAssetResolver<ZipEntry> {
	static String QUOTED_FILE_SEPARATOR = Pattern.quote("/")
	static String DIRECTIVE_FILE_SEPARATOR = '/'

	JarFile baseJar
	String prefixPath

	JarAssetResolver(String name,String jarPath, String prefixPath) {
		super(name)
		try {
			baseJar = new JarFile(jarPath)
		} catch (ZipException x) {
			log.error "ERROR: Jar file is corrupted ${jarPath}"
			throw x
		}
		this.prefixPath = prefixPath
	}

	AssetFile getAsset(String relativePath, String contentType = null, String extension = null, AssetFile baseFile=null) {
		if(!relativePath) {
			return null
		}
		if(relativePath.startsWith('/')) {
			relativePath = relativePath.substring(1)
		}
		def normalizedPath = AssetHelper.normalizePath(relativePath)
		def specs

		if(contentType) {
			specs = AssetHelper.getPossibleFileSpecs(contentType)
		} else {
			if(!extension) {
				extension = AssetHelper.extensionFromURI(relativePath)
			}
			specs = AssetHelper.assetFileClasses().findAll { it.extensions.contains(extension) }
		}


        AssetFile assetFile = resolveAsset(specs, prefixPath, normalizedPath, baseFile, extension)

		return assetFile
	}


    protected Closure<InputStream> createInputStreamClosure(ZipEntry file) {
        if(file) {
			return {-> new BufferedInputStream(baseJar.getInputStream(file),512) }
		}
		return null
    }

    @CompileStatic
	public List<AssetFile> getAssets(String basePath, String contentType = null, String extension = null,  Boolean recursive = true, AssetFile relativeFile=null, AssetFile baseFile = null) {
		def fileList = []

		if(!basePath.startsWith('/') && relativeFile != null) {
			List<String> pathArgs = relativeFile.parentPath ? relativeFile.parentPath.split(DIRECTIVE_FILE_SEPARATOR).toList() as List<String> : [] as List<String> //(path should be relative not canonical)
			String[] basePathArgs = basePath.split(DIRECTIVE_FILE_SEPARATOR)
			List<String> parentPathArgs = pathArgs ? pathArgs[0..(pathArgs.size() - 1)] as List<String> : [] as List<String>
 			parentPathArgs.addAll(basePathArgs.toList() as List<String>)
			parentPathArgs = (parentPathArgs).findAll{String it -> it != "."} as List<String>
			basePath = parentPathArgs.join(DIRECTIVE_FILE_SEPARATOR)
		}
		def combinedPath = basePath ? [prefixPath, basePath].join("/") : prefixPath
		basePath = AssetHelper.normalizePath(combinedPath) + "/"

		baseJar.entries().each { JarEntry entry ->
			if(entry.name.startsWith(basePath)) {
				String[] mimeType = AssetHelper.assetMimeTypeForURI(entry.name)
				if(!entry.isDirectory() && mimeType && contentType in mimeType) {
					fileList << assetForFile(entry,contentType, baseFile, prefixPath)
				}
			}
		}

		return fileList
	}

    @Override
    @CompileStatic
    protected String getFileName(ZipEntry file) {
        return file.name
    }

    @CompileStatic
    protected ZipEntry getRelativeFile(String relativePath, String name) {
		return baseJar.getEntry([relativePath, name].join("/"))
	}

    @CompileStatic
	protected String relativePathToResolver(ZipEntry file, String scanDirectoryPath) {
		def filePath = file.name

		if(filePath.startsWith(scanDirectoryPath)) {
			return filePath.substring(scanDirectoryPath.size() + 1).replace(QUOTED_FILE_SEPARATOR, DIRECTIVE_FILE_SEPARATOR)
		} else {
			throw new RuntimeException("File was not sourced from the same ScanDirectory #{filePath}")
		}
	}

	/**
	* Uses file globbing to scan for files that need precompiled
	*/
	public Collection<AssetFile> scanForFiles(List<String> excludePatterns, List<String> includePatterns) {
		def fileList = []
		List<String> excludedPatternList =  excludePatterns ? excludePatterns : new ArrayList<String>()
        List<String> includedPatternList =  includePatterns ? includePatterns : new ArrayList<String>()

		for(JarEntry entry in baseJar.entries()) {
			if(entry.name.startsWith(prefixPath + "/")) {
				def relativePath = relativePathToResolver(entry, prefixPath)
				if(!isFileMatchingPatterns(relativePath,excludedPatternList) || isFileMatchingPatterns(relativePath,includedPatternList)) {
					if(!entry.isDirectory()) {
						def assetFileClass = AssetHelper.assetForFileName(relativePath)
						if(assetFileClass) {
							fileList << assetFileClass.newInstance(inputStreamSource: createInputStreamClosure(entry), path: relativePath, sourceResolver: this)
						} else {
							fileList << new GenericAssetFile(inputStreamSource: createInputStreamClosure(entry), path: relativePath)
						}
					}
				}
			}
		}

		return fileList.unique { AssetFile a,  AssetFile b -> a.path <=> b.path }
	}

}

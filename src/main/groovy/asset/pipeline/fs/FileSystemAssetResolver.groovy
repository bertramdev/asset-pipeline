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

import java.util.regex.Pattern

/**
 * Implementation of the {@link AssetResolver} interface for the file system
 *
 * @author David Estes
 * @author Graeme Rocher
 */

@Commons
class FileSystemAssetResolver extends AbstractAssetResolver {
	static String QUOTED_FILE_SEPARATOR = Pattern.quote(File.separator)
	static String DIRECTIVE_FILE_SEPARATOR = '/'

	File baseDirectory
	List<String> scanDirectories = []

	FileSystemAssetResolver(String name,String basePath, boolean flattenSubDirectories=true) {
		this.name = name
		baseDirectory = new File(basePath)
		if(baseDirectory.exists()) {
			if(flattenSubDirectories) {
				def scopedDirectories = baseDirectory.listFiles()
				for(scopedDirectory in scopedDirectories) {
					if(scopedDirectory.isDirectory() && scopedDirectory.getName() != "WEB-INF" && scopedDirectory.getName() != 'META-INF') {
						scanDirectories << scopedDirectory.canonicalPath
					}
				}
			} else {
				scanDirectories << baseDirectory.canonicalPath
			}

		}
		log.debug "Asset Pipeline FSResolver Initialized with Scan Directories: ${scanDirectories}"
	}


	//TODO: WINDOWS SUPPORT USE QUOTED FILE SEPARATORS
	public AssetFile getAsset(String relativePath, String contentType = null, String extension = null, AssetFile baseFile=null) {
		if(!relativePath) {
			return null
		}
		relativePath = relativePath.replace(QUOTED_FILE_SEPARATOR,DIRECTIVE_FILE_SEPARATOR)
		def specs
		if(contentType) {
			specs = AssetHelper.getPossibleFileSpecs(contentType)
		}

		for(directoryPath in scanDirectories) {
			if(specs) {
				for(fileSpec in specs) {
					def fileName = relativePath
					if(fileName.endsWith(".${fileSpec.compiledExtension}")) {
						fileName = fileName.substring(0,fileName.lastIndexOf(".${fileSpec.compiledExtension}"))
					}
					for(ext in fileSpec.extensions) {
						def tmpFileName = fileName
						if(!tmpFileName.endsWith("." + ext)) {
							tmpFileName += "." + ext
						}
						def file = new File(directoryPath, tmpFileName)
						if(file.exists()) {
							return fileSpec.newInstance(inputStreamSource: { file.newInputStream() }, baseFile: baseFile, path: relativePathToResolver(file,directoryPath), sourceResolver: this)
						}
					}
				}
			} else {
				def fileName = relativePath
				if(extension) {
					if(!fileName.endsWith(".${extension}")) {
						fileName += ".${extension}"
					}
				}
				def file = new File(directoryPath, fileName)
				if(file.exists()) {
					return new GenericAssetFile(inputStreamSource: { file.newInputStream() }, path: relativePathToResolver(file,directoryPath))
				}
			}
		}
		return null
	}


	/**
	* Implementation Requirements
	* Should be able to take a relative to baseFile scenario
	*/
    @CompileStatic
	public List<AssetFile> getAssets(String basePath, String contentType = null, String extension = null,  Boolean recursive = true, AssetFile relativeFile = null, AssetFile baseFile = null) {
		//We are going absolute
        List<AssetFile> fileList = []

		if(!basePath.startsWith('/') && relativeFile != null) {
			List<String> pathArgs = relativeFile.parentPath ? relativeFile.parentPath.split(DIRECTIVE_FILE_SEPARATOR).toList() : new ArrayList<String>() //(path should be relative not canonical)
			def basePathArgs = basePath.split(DIRECTIVE_FILE_SEPARATOR)
			def parentPathArgs = pathArgs ? pathArgs[0..(pathArgs.size() - 1)] : []
			parentPathArgs.addAll(basePathArgs.toList())
			basePath = (parentPathArgs).join(File.separator)
		}

		for(directoryPath in scanDirectories) {
			def file = new File(directoryPath,basePath)
			if(file.exists() && file.isDirectory()) {
				recursiveTreeAppend(file, fileList, contentType,baseFile,recursive, directoryPath)
			}
		}
		return fileList
	}

    @CompileStatic
	protected void recursiveTreeAppend(File directory, List<AssetFile> tree, String contentType=null, AssetFile baseFile, boolean recursive=true, String sourceDirectory) {
		def files = directory.listFiles()
		files = files?.sort { File a, File b -> a.name.compareTo b.name }
		for(file in files) {
			if(file.isDirectory() && recursive) {
				recursiveTreeAppend(file,tree, contentType, baseFile, recursive, sourceDirectory)
			}
			else if(contentType in AssetHelper.assetMimeTypeForURI(file.getAbsolutePath())) {
				tree << assetForFile(file,contentType, baseFile, sourceDirectory)
			}
		}
	}

	protected AssetFile assetForFile(File file, String contentType, AssetFile baseFile=null, String sourceDirectory) {
		if(file == null) {
			return null
		}

		if(contentType == null) {
			return new GenericAssetFile(inputStreamSource: { file.newInputStream() }, path: relativePathToResolver(file,sourceDirectory))
		}

		def possibleFileSpecs = AssetHelper.getPossibleFileSpecs(contentType)
		for(fileSpec in possibleFileSpecs) {
			for(extension in fileSpec.extensions) {
				def fileName = file.getAbsolutePath()
				if(fileName.endsWith("." + extension)) {
					return fileSpec.newInstance(inputStreamSource: { file.newInputStream() }, baseFile: baseFile, path: relativePathToResolver(file,sourceDirectory), sourceResolver: this)
				}
			}
		}
        return new GenericAssetFile(inputStreamSource: { file.newInputStream() }, path: relativePathToResolver(file,sourceDirectory))
	}

    @CompileStatic
	protected String relativePathToResolver(File file, String scanDirectoryPath) {
		def filePath = file.canonicalPath

		if(filePath.startsWith(scanDirectoryPath)) {
			return filePath.substring(scanDirectoryPath.size() + 1).replace(QUOTED_FILE_SEPARATOR, DIRECTIVE_FILE_SEPARATOR)
		} else {
			for(scanDir in scanDirectories) {
				if(filePath.startsWith(scanDir)) {
					return filePath.substring(scanDir.size() + 1).replace(QUOTED_FILE_SEPARATOR, DIRECTIVE_FILE_SEPARATOR)
				}
			}
			throw new RuntimeException("File was not sourced from the same ScanDirectory #{filePath}")
		}
	}


	/**
	* Uses file globbing to scan for files that need precompiled
	*/
    @CompileStatic
	public Collection<AssetFile> scanForFiles(List<String> excludePatterns, List<String> includePatterns) {
		List<AssetFile> fileList = []
		List<Pattern> excludedPatternRegex =  excludePatterns ? excludePatterns.collect{ convertGlobToRegEx(it) } : new ArrayList<Pattern>()
        List<Pattern> includedPatternRegex =  includePatterns ? includePatterns.collect{ convertGlobToRegEx(it) } : new ArrayList<Pattern>()

		for(String scanDirectory in scanDirectories) {
			def scanPath = new File(scanDirectory)
			iterateOverFileSystem(scanPath,excludedPatternRegex,includedPatternRegex, fileList, scanDirectory)
		}

		return fileList.unique { AssetFile a, AssetFile b -> a.path <=> b.path }
	}

    @CompileStatic
	protected iterateOverFileSystem(File dir, List<Pattern> excludePatterns, List<Pattern> includePatterns, List<AssetFile> fileList, String sourcePath) {
		dir.listFiles()?.each { File file ->
			def relativePath = relativePathToResolver(file, sourcePath)
			if(!isFileMatchingPatterns(relativePath,excludePatterns) || isFileMatchingPatterns(relativePath,includePatterns)) {
				if(file.isDirectory()) {
					iterateOverFileSystem(file,excludePatterns, includePatterns, fileList, sourcePath)
				} else {
					def assetFileClass = AssetHelper.assetForFileName(relativePath)
					if(assetFileClass) {
						fileList << assetFileClass.newInstance(inputStreamSource: { file.newInputStream() }, baseFile: null, path: relativePath, sourceResolver: this)
					} else {
						fileList << new GenericAssetFile(inputStreamSource: { file.newInputStream() }, path: relativePath)
					}
				}

			}
		}
	}


}

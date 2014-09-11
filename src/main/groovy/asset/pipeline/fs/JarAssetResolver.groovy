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
import java.util.regex.Pattern
import groovy.util.logging.Log4j
import java.util.jar.JarFile

@Log4j
class JarAssetResolver extends AbstractAssetResolver {
	static QUOTED_FILE_SEPARATOR = Pattern.quote("/")
	static DIRECTIVE_FILE_SEPARATOR = '/'

	JarFile baseJar
	String prefixPath

	JarAssetResolver(String name,String jarPath, String prefixPath) {
		this.name = name
		baseJar = new JarFile(jarPath)
		this.prefixPath = prefixPath
	}

	public def getAsset(String relativePath, String contentType = null, String extension = null, AssetFile baseFile=null) {
		if(!relativePath) {
			return null
		}
		def normalizedPath = AssetHelper.normalizePath(relativePath)
		def specs
		if(contentType) {
			specs = AssetHelper.getPossibleFileSpecs(contentType)
		}


			if(specs) {
				for(fileSpec in specs) {
					def fileName = normalizedPath
					if(fileName.endsWith(".${fileSpec.compiledExtension}")) {
						fileName = fileName.substring(0,fileName.lastIndexOf(".${fileSpec.compiledExtension}"))
					}
					for(ext in fileSpec.extensions) {
						def tmpFileName = fileName
						if(!tmpFileName.endsWith("." + ext)) {
							tmpFileName += "." + ext
						}
						def file = getEntry(tmpFileName)
						if(file) {
							return fileSpec.newInstance(inputStreamSource: { baseJar.getInputStream(file) }, baseFile: baseFile, path: relativePathToResolver(file,prefixPath), sourceResolver: this)
						}
					}
				}
			} else {
				def fileName = normalizedPath
				if(extension) {
					if(!fileName.endsWith(".${extension}")) {
						fileName += ".${extension}"
					}
				}
				def file = getEntry(fileName)

				if(file) {
					return new GenericAssetFile(inputStreamSource: { baseJar.getInputStream(file) }, path: relativePathToResolver(file,directoryPath))
				}
			}

		return null
	}

	public def getAssets(String basePath, String contentType = null, String extension = null,  Boolean recursive = true, AssetFile relativeFile=null, AssetFile baseFile = null) {
		def fileList = []

		if(!basePath.startsWith('/') && relativeFile != null) {
			def pathArgs = relativeFile.parentPath ? relativeFile.parentPath.split(DIRECTIVE_FILE_SEPARATOR) : [] //(path should be relative not canonical)
			def basePathArgs = basePath.split(DIRECTIVE_FILE_SEPARATOR)
			def parentPathArgs = pathArgs ? pathArgs[0..(pathArgs.size() - 1)] : []
			parentPathArgs.addAll(basePathArgs)
			parentPathArgs = (parentPathArgs).findAll{it != "."}
			basePath = parentPathArgs.join(File.separator)
		}
		def combinedPath = basePath ? [prefixPath, basePath].join("/") : prefixPath
		basePath = AssetHelper.normalizePath(combinedPath + "/")

		baseJar.entries().each { entry ->
			if(entry.name.startsWith(basePath)) {

				if(!entry.isDirectory() && contentType in AssetHelper.assetMimeTypeForURI(entry.name)) {
					fileList << assetForFile(entry,contentType, baseFile, prefixPath)
				}
			}
		}

		return fileList
	}

	def assetForFile(file,contentType, baseFile=null, sourceDirectory) {
		if(file == null) {
			return null
		}

		if(contentType == null) {
			return new GenericAssetFile(inputStreamSource: { baseJar.getInputStream(file) }, path: relativePathToResolver(file,sourceDirectory))
		}

		def possibleFileSpecs = AssetHelper.getPossibleFileSpecs(contentType)
		for(fileSpec in possibleFileSpecs) {
			for(extension in fileSpec.extensions) {
				def fileName = file.name
				if(fileName.endsWith("." + extension)) {
					return fileSpec.newInstance(inputStreamSource: { baseJar.getInputStream(file) }, baseFile: baseFile, path: relativePathToResolver(file,sourceDirectory), sourceResolver: this)
				}
			}
		}
		return file
	}

	def getEntry(String name) {
		return baseJar.getEntry([prefixPath, name].join("/"))
	}

	def relativePathToResolver(file, scanDirectoryPath) {
		def filePath = file.name

		if(filePath.startsWith(scanDirectoryPath)) {
			return filePath.substring(scanDirectoryPath.size() + 1).replace(QUOTED_FILE_SEPARATOR, DIRECTIVE_FILE_SEPARATOR)
		} else {
			throw RuntimeException("File was not sourced from the same ScanDirectory #{filePath}")
		}
	}

}

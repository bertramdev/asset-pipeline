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

import groovy.util.logging.Commons
import asset.pipeline.processors.ClosureCompilerProcessor
import asset.pipeline.processors.CssMinifyPostProcessor


/**
 * Build time compiler for assets
 *
 * @author David Estes
 * @author Graeme Rocher
 */
@Commons
class AssetCompiler {
	def includeRules = [:]
	def excludeRules = [:]

	Map options = [:]
	def eventListener
	def filesToProcess = []
	Properties manifestProperties

	AssetCompiler(options=[:], eventListener = null) {
		this.eventListener = eventListener
		this.options = options
		if(!options.compileDir) {
			options.compileDir = "target/assets"
		}
		if(!options.excludesGzip) {
			options.excludesGzip = ['png', 'jpg','jpeg', 'gif', 'zip', 'gz']
		} else {
			options.excludesGzip += ['png', 'jpg','jpeg', 'gif', 'zip', 'gz']
		}

		if(!options.containsKey('enableGzip')) {
			options.enableGzip = true
		}

		if(!options.containsKey('enableDigests')) {
			options.enableDigests = true
		}
		if(!options.containsKey('skipNonDigests')) {
			options.skipNonDigests = false
		}
		// Load in additional assetSpecs
		options.specs?.each { spec ->
			def specClass = this.class.classLoader.loadClass(spec)
			if(specClass) {
				AssetHelper.assetSpecs << (Class<AssetFile>)specClass
			}
		}
		manifestProperties = new Properties()
	}


	/**
	* Main Target Endpoint for Launching The AssetCompile in a Forked Execution Environment
	* Arguments
	* -o compileDir
	* -i sourceDir (List of SourceDirs)
	* -j List of Source Jars (, delimited)
	* -d Digests
	* -z Compression
	* -m SourceMaps
	* -n Skip Non Digests
	* -c Config Location
	* command - compile,watch
	*/
	static void main(String[] args) {
		def properties = new java.util.Properties()
		System.properties.each { k,v ->
			if(k.startsWith('asset.pipeline')) {
				def newKey = k.substring('asset.pipeline'.size())
				println "Key ${k} - ${v}"
			}
		}
		// def properties = System.getProperties()
		def assetCompiler = new AssetCompiler()
	}

	void compile() {
		def assetDir           = initializeWorkspace()

		def minifyCssProcessor = new CssMinifyPostProcessor()

		filesToProcess = this.getAllAssets()
		// Lets clean up assets that are no longer being compiled
		removeDeletedFiles(filesToProcess)

		for(int index = 0 ; index < filesToProcess.size() ; index++) {
			def assetFile = filesToProcess[index]
			def fileName = assetFile.path
			def startTime = new Date().time
			eventListener?.triggerEvent("StatusUpdate", "Processing File ${index+1} of ${filesToProcess.size()} - ${fileName}")

			def digestName
			def isUnchanged    = false
			def extension      = AssetHelper.extensionFromURI(fileName)
			fileName           = AssetHelper.nameWithoutExtension(fileName)
			def fileSystemName = fileName.replace(AssetHelper.DIRECTIVE_FILE_SEPARATOR, File.separator)


			if(assetFile) {
				def fileData
				if(!(assetFile instanceof GenericAssetFile)) {
					if(assetFile.compiledExtension) {
						extension = assetFile.compiledExtension
						fileName = AssetHelper.fileNameWithoutExtensionFromArtefact(fileName,assetFile)
					}
					def contentType = (assetFile.contentType instanceof String) ? assetFile.contentType : assetFile.contentType[0]
					def directiveProcessor = new DirectiveProcessor(contentType, this, options.classLoader)
					fileData   = directiveProcessor.compile(assetFile)
					digestName = AssetHelper.getByteDigest(fileData.bytes)
					def existingDigestFile = manifestProperties.getProperty("${fileName}.${extension}")
					if(existingDigestFile && existingDigestFile == "${fileName}-${digestName}.${extension}") {
						isUnchanged=true
					}
					if(fileName.indexOf(".min") == -1 && contentType == 'application/javascript' && options.minifyJs && !isUnchanged) {
						def newFileData = fileData
						try {
							def closureCompilerProcessor = new ClosureCompilerProcessor(this)
							eventListener?.triggerEvent("StatusUpdate", "Uglifying File ${index+1} of ${filesToProcess.size()} - ${fileName}")
							newFileData = closureCompilerProcessor.process(fileName,fileData, options.minifyOptions ?: [:])
						} catch(e) {
							log.error("Uglify JS Exception", e)
							newFileData = fileData
						}
						fileData = newFileData
					} else if(fileName.indexOf(".min") == -1 && contentType == 'text/css' && options.minifyCss && !isUnchanged) {
						def newFileData = fileData
						try {
							eventListener?.triggerEvent("StatusUpdate", "Minifying File ${index+1} of ${filesToProcess.size()} - ${fileName}")
							newFileData = minifyCssProcessor.process(fileData)
						} catch(e) {
							log.error("Minify CSS Exception", e)
							newFileData = fileData
						}
						fileData = newFileData
					}

					if(assetFile.encoding) {
						fileData = fileData.getBytes(assetFile.encoding)
					} else {
						fileData = fileData.bytes
					}

				} else {
					digestName = AssetHelper.getByteDigest(assetFile.bytes)
					def existingDigestFile = manifestProperties.getProperty("${fileName}.${extension}")
					if(existingDigestFile && existingDigestFile == "${fileName}-${digestName}.${extension}") {
						isUnchanged=true
					}
				}

				if(!isUnchanged) {
					def outputFileName = fileName
					if(extension) {
						outputFileName = "${fileSystemName}.${extension}"
					}
					def outputFile = new File(options.compileDir, "${outputFileName}")

					def parentTree = new File(outputFile.parent)
					parentTree.mkdirs()

					byte[] outputBytes
					if(fileData) {
						outputBytes = fileData

					} else {
						if(assetFile instanceof GenericAssetFile) {
							outputBytes = assetFile.bytes
						} else {
							outputBytes = assetFile.inputStream.bytes
							digestName = AssetHelper.getByteDigest(assetFile.inputStream.bytes)
						}
					}
					if(!options.skipNonDigests) {
						outputFile.createNewFile()
						outputFile.bytes = outputBytes
					}

					if(extension) {
						try {
							def digestedFile
							if(options.enableDigests) {
								digestedFile = new File(options.compileDir,"${fileSystemName}-${digestName}${extension ? ('.' + extension) : ''}")
								digestedFile.createNewFile()
								digestedFile.bytes = outputBytes
								manifestProperties.setProperty("${fileName}.${extension}", "${fileName}-${digestName}${extension ? ('.' + extension) : ''}")
							}

							// Zip it Good!
							if(options.enableGzip == true && !options.excludesGzip.find{ it.toLowerCase() == extension.toLowerCase()}) {
								eventListener?.triggerEvent("StatusUpdate","Compressing File ${index+1} of ${filesToProcess.size()} - ${fileName}")
								createCompressedFiles(outputFile,outputBytes, digestedFile)
							}
						} catch(ex) {
							log.error("Error Compiling File ${fileName}.${extension}",ex)
						}
					}
				}

			}
		}

		saveManifest()
		eventListener?.triggerEvent("StatusUpdate","Finished Precompiling Assets")
  }

  private initializeWorkspace() {
		 // Check for existing Compiled Assets
	  def assetDir = new File(options.compileDir)
	  if(assetDir.exists()) {
		def manifestFile = new File(options.compileDir,"manifest.properties")
		if(manifestFile.exists())
			manifestProperties.load(manifestFile.newDataInputStream())
	  } else {
		assetDir.mkdirs()
	  }
	  return assetDir
  }

	def getIncludesForPathKey(String key) {
		def includes = []
		def defaultIncludes = includeRules.default
		if(defaultIncludes) {
			includes += defaultIncludes
		}
		if(includeRules[key]) {
			includes += includeRules[key]
		}
		return includes.unique()
	}

	def getExcludesForPathKey(String key) {
		def excludes = ["**/.*","**/.DS_Store", 'WEB-INF/**/*', '**/META-INF/*', '**/_*.*','**/.svn/**']
		def defaultExcludes = excludeRules.default
		if(defaultExcludes) {
			excludes += defaultExcludes
		}
		if(excludeRules[key]) {
			excludes += excludeRules[key]
		}

		return excludes.unique()
	}


	def getAllAssets() {
		def filesToProcess = []
		AssetPipelineConfigHolder.resolvers.each { resolver ->
			def files = resolver.scanForFiles(getExcludesForPathKey(resolver.name),getIncludesForPathKey(resolver.name))
			filesToProcess += files
		}

		filesToProcess.unique{ a,b -> a.path <=> b.path}
		return filesToProcess //Make sure we have a unique set
	}

	private saveManifest() {
		// Update Manifest
		def manifestFile = new File(options.compileDir,'manifest.properties')
		manifestProperties.store(manifestFile.newWriter(),"")
	}

	@groovy.transform.CompileStatic
	private void createCompressedFiles(File outputFile, byte[] outputBytes, File digestedFile) {
		java.io.ByteArrayOutputStream targetStream  = new java.io.ByteArrayOutputStream()
		java.util.zip.GZIPOutputStream zipStream     = new java.util.zip.GZIPOutputStream(targetStream)

		zipStream.write(outputBytes)
		zipStream.finish()
		byte[] zipBytes = targetStream.toByteArray()
		if(!options.skipNonDigests) {
			File zipFile = new File("${outputFile.getAbsolutePath()}.gz")
			zipFile.createNewFile()
			zipFile.bytes = zipBytes
		}

		if(options.enableDigests as Boolean) {
			File zipFileDigest = new File("${digestedFile.getAbsolutePath()}.gz")
			zipFileDigest.createNewFile()
			zipFileDigest.bytes = zipBytes
		}

		targetStream.close()
	}

	private removeDeletedFiles(filesToProcess) {
		def compiledFileNames = filesToProcess.collect { assetFile ->
			def fileName  = assetFile.path
			def extension   = AssetHelper.extensionFromURI(fileName)
			fileName        = AssetHelper.nameWithoutExtension(fileName)

			if(assetFile && !(assetFile instanceof GenericAssetFile) && assetFile.compiledExtension) {
				extension = assetFile.compiledExtension
				fileName = AssetHelper.fileNameWithoutExtensionFromArtefact(fileName,assetFile)
			}
			return "${fileName}.${extension}"
		}

		def propertiesToRemove = []
		manifestProperties.keySet().each { compiledUri ->
			def compiledName = 	compiledUri//.replace(AssetHelper.DIRECTIVE_FILE_SEPARATOR,File.separator)

			def fileFound = compiledFileNames.find{ it == compiledName.toString()}
			if(!fileFound) {
				def digestedUri = manifestProperties.getProperty(compiledName)
				def digestedName = digestedUri//.replace(AssetHelper.DIRECTIVE_FILE_SEPARATOR,File.separator)
				def compiledFile = new File(options.compileDir, compiledName)
				def digestedFile = new File(options.compileDir, digestedName)
				def zippedFile = new File(options.compileDir, "${compiledName}.gz")
				def zippedDigestFile = new File(options.compileDir, "${digestedName}.gz")
				if(compiledFile.exists()) {
					compiledFile.delete()
				}
				if(digestedFile.exists()) {
					digestedFile.delete()
				}
				if(zippedFile.exists()) {
					zippedFile.delete()
				}
				if(zippedDigestFile.exists()) {
					zippedDigestFile.delete()
				}
				propertiesToRemove << compiledName
			}
		}

		propertiesToRemove.each {
			manifestProperties.remove(it)
		}
	}
}

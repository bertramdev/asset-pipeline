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


import java.util.regex.Pattern
import java.security.MessageDigest
import java.nio.channels.FileChannel

/**
 * Helper class for resolving assets
 *
 * @author David Estes
 * @author Graeme Rocher
 */
class AssetHelper {
    static final Collection<Class<AssetFile>> assetSpecs = [JsAssetFile, CssAssetFile]
    static final String QUOTED_FILE_SEPARATOR = Pattern.quote(File.separator)
    static final String DIRECTIVE_FILE_SEPARATOR = '/'

    /**
     * Resolve an {@link AssetFile} for the given URI
     *
     * @param uri The URI
     * @param contentType The content type
     * @param ext The extension
     * @param baseFile The base file
     * @return
     */
    static AssetFile fileForUri(String uri, String contentType=null, String ext=null, AssetFile baseFile=null) {
        AssetFile file
        for(resolver in AssetPipelineConfigHolder.resolvers) {
            file = resolver.getAsset(uri,contentType,ext,baseFile)
            if(file) {
                return file
            }
        }
        return null
    }

    /**
     * @return The classes that implement the {@link AssetFile} interface
     */
    static Collection<Class<AssetFile>> assetFileClasses() {
        return assetSpecs
    }

    static AssetFile assetForFile(file, String contentType, AssetFile baseFile=null) {
        if(contentType == null || file == null) {
            return file
        }

        def possibleFileSpecs = getPossibleFileSpecs(contentType)
        for(fileSpec in possibleFileSpecs) {
            for(extension in fileSpec.extensions) {
                def fileName = file.getAbsolutePath()
                if(fileName.endsWith("." + extension)) {
                    return fileSpec.newInstance(file: file, baseFile:baseFile)
                }
            }
        }

        return file
    }

    /**
    * Finds the AssetFile definition for the specified file name based on its extension
    * @param filename String filename representation
    */
    static Class<AssetFile> assetForFileName(filename) {
        return assetFileClasses().find{ fileClass ->
            fileClass.extensions.find { filename.endsWith(".${it}") }
        }
    }

    /**
     * Obtains an {@link AssetFile} instance for the given URI
     *
     * @param uri The given URI
     * @return The AssetFile instance or null if non exists
     */
    static AssetFile fileForFullName(String uri) {
        for(resolver in AssetPipelineConfigHolder.resolvers) {
            def file = resolver.getAsset(uri)
            if(file) {
                return file
            }
        }
        return null
    }

    /**
     * Obtains the extension for the given URI
     *
     * @param uri The URI
     * @return The extension or null
     */
    static String extensionFromURI(String uri) {
        def uriComponents = uri.split("/")
        def lastUriComponent = uriComponents[uriComponents.length - 1]
        String extension = null
        if(lastUriComponent.lastIndexOf(".") >= 0) {
            extension = uri.substring(uri.lastIndexOf(".") + 1)
        }
        return extension
    }

    /**
     * Obtains the name of the file sans the extension
     *
     * @param uri The URI
     * @return The name of the file without extension
     */
    static String nameWithoutExtension(String uri) {
        def uriComponents = uri.split("/")
        def lastUriComponent = uriComponents[uriComponents.length - 1]
        if(lastUriComponent.lastIndexOf(".") >= 0) {
            return uri.substring(0,uri.lastIndexOf("."))
        }
        return uri
    }

    static String fileNameWithoutExtensionFromArtefact(String filename, AssetFile assetFile) {
        if(assetFile == null) {
            return null
        }

        def rootName = filename
        assetFile.extensions.each { extension ->

            if(filename.endsWith(".${extension}")) {
                def potentialName = filename.substring(0,filename.lastIndexOf(".${extension}"))
                if(potentialName.length() < rootName.length()) {
                    rootName = potentialName
                }
            }
        }
        return rootName
    }

    /**
     * The asset content type for the given URI
     *
     * @param uri The URI
     * @return
     */
    static List<String> assetMimeTypeForURI(String uri) {
        def fileSpec = assetForFileName(uri)
        if(fileSpec) {
            if(fileSpec.contentType instanceof String) {
                return [fileSpec.contentType]
            }
            return fileSpec.contentType
        }
        return null
    }

    /**
    * Copies a files contents from one file to another and flushes.
    * Note: We use FileChannel instead of FileUtils.copyFile to ensure a synchronous forced save.
    * This helps ensures files exist on the disk before a war file is created.
    * @param sourcceFile the originating file we want to copy
    * @param destFile the destination file object we want to save to
    */
    static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile()
        }

         FileChannel source = null
         FileChannel destination = null
        try {
            source = new FileInputStream(sourceFile).getChannel()
            destination = new FileOutputStream(destFile).getChannel()
            destination.transferFrom(source, 0, source.size())
            destination.force(true)
        }
        finally {
            source?.close()
            destination?.close()
        }
    }

    /**
     *
     * @param uri the string of the asset uri.
     * @param possibleFileSpecs is a list of possible file specs that the file for the uri can belong to.
     * @return an AssetFile for the corresponding uri.
     */
    static AssetFile fileForUriIfHasAnyAssetType(String uri, Collection<Class<AssetFile>> possibleFileSpecs, baseFile=null) {
        for(fileSpec in possibleFileSpecs) {
            for(extension in fileSpec.extensions) {
                def fullName = uri
                if(fullName.endsWith(".${fileSpec.compiledExtension}")) {
                    fullName = fullName.substring(0,fullName.lastIndexOf(".${fileSpec.compiledExtension}"))
                }
                if(!fullName.endsWith("." + extension)) {
                    fullName += "." + extension
                }

                def file = fileForFullName(fullName)
                if(file) {
                    return fileSpec.newInstance(file: file, baseFile: baseFile)
                }
            }
        }
    }

    /**
     *
     * @param uri string representation of the asset file.
     * @param ext the extension of the file
     * @return An instance of the file that the uri belongs to.
     */
    static getAssetFileWithExtension(String uri, String ext) {
        def fullName = uri
        if(ext) {
           fullName = uri + "." + ext
        }
        def assetFile = AssetHelper.fileForFullName(fullName)
        if(assetFile) {
            return assetFile
        }
    }

    /**
     * Returns the possible {@link AssetFile} classes for the given content type
     *
     * @param contentType The content type
     * @return The {@link AssetFile} classes
     */
    static Collection<Class<AssetFile>> getPossibleFileSpecs(String contentType) {
        return assetFileClasses().findAll { (it.contentType instanceof String) ? it.contentType == contentType : contentType in it.contentType }
    }

    /**
    * Generates an MD5 Byte Digest from a byte array
    * @param fileBytes byte[] array of the contents of a file
    * @return md5 String
    */
    static String getByteDigest(byte[] fileBytes) {
        // Generate Checksum based on the file contents and the configuration settings
        MessageDigest md = MessageDigest.getInstance("MD5")
        md.update(fileBytes)
        def checksum = md.digest()
        return checksum.encodeHex().toString()
    }


    /**
     * Normalizes a path into a standard path, stripping out all path elements that walk the path (i.e. '..' and '.')
     * @param path String path (i.e. '/path/to/../file.js')
     * @return normalied path String (i.e. '/path/file.js')
    */
    static String normalizePath(String path) {
        def pathArgs = path.split("/")
        def newPath = []
        for(int counter=0;counter < pathArgs.length; counter++) {
            def pathElement = pathArgs[counter]
            if(pathElement == '..') {
                if(newPath.size() > 0) {
                    newPath = newPath[0..(newPath.size() - 2)]
                }
            } else if(pathElement == '.') {
                // do nothing
            } else {
                newPath << pathElement
            }
        }
        return newPath.join("/")
    }


}

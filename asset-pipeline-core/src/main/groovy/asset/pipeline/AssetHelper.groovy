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

import java.nio.file.FileSystems
import java.nio.file.PathMatcher
import java.nio.file.Paths
import java.util.regex.Pattern
import java.security.MessageDigest
import java.nio.channels.FileChannel
import groovy.transform.CompileStatic

/**
 * Helper class for resolving assets
 *
 * @author David Estes
 * @author Graeme Rocher
 */
public class AssetHelper {
    static final Collection<Class<AssetFile>> assetSpecs = AssetSpecLoader.loadSpecifications()
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
    static AssetFile fileForUri(String uri, String contentType = null, String ext = null, AssetFile baseFile = null) {
        AssetFile file
        for (resolver in AssetPipelineConfigHolder.resolvers) {
            file = resolver.getAsset(uri, contentType, ext, baseFile)
            if (file) {
                return file
            }
        }
        return null
    }

    /**
     * @return The classes that implement the {@link AssetFile} interface
     */
    static Collection<Class<AssetFile>> assetFileClasses() {
        return assetSpecs;
    }

    /**
     * Finds the AssetFile definition for the specified file name based on its extension
     * @param filename String filename representation
     */
    static Class<AssetFile> assetForFileName(String filename) {
        Map<String, Class<AssetFile>> extensionMap = [:]
        for (fileSpec in assetFileClasses()) {
            for (extension in fileSpec.extensions) {
                if (extensionMap[extension] == null) {
                    extensionMap[extension] = fileSpec
                }
            }
        }

        List<String> extensions = extensionMap.keySet().sort(false) { String a, String b -> -(a.size()) <=> -(b.size()) }
        String matchedExtension = extensions.find { filename.endsWith(".${it}".toString()) }
        if (matchedExtension) {
            return extensionMap[matchedExtension]
        } else {
            return null
        }
    }

    /**
     * Obtains an {@link AssetFile} instance for the given URI
     *
     * @param uri The given URI
     * @return The AssetFile instance or null if non exists
     */
    static AssetFile fileForFullName(String uri) {
        for (resolver in AssetPipelineConfigHolder.resolvers) {
            AssetFile file = resolver.getAsset(uri)
            if (file) {
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
        String[] uriComponents = uri.split("/")
        if (uriComponents.length == 0) {
            return null
        }
        String lastUriComponent = uriComponents[uriComponents.length - 1]
        List<String> extensions = (List<String>) (AssetHelper.assetSpecs.collect { Class<AssetFile> it -> it.extensions }.flatten().sort(false) { String a, String b -> -(a.size()) <=> -(b.size()) })
        String extension = null
        extension = extensions.find { lastUriComponent.endsWith(".${it}".toString()) }
        if (!extension) {
            if (lastUriComponent.lastIndexOf(".") >= 0) {
                extension = uri.substring(uri.lastIndexOf(".") + 1)
            }
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
        String[] uriComponents = uri.split("/")
        String lastUriComponent = uriComponents[uriComponents.length - 1]
        String extension = extensionFromURI(lastUriComponent)
        if (extension) {
            return uri.substring(0, uri.lastIndexOf(".${extension}"))
        }
        return uri
    }


    static String fileNameWithoutExtensionFromArtefact(String filename, AssetFile assetFile) {
        if (assetFile == null) {
            return null
        }

        String rootName = filename
        assetFile.extensions.sort(false) { String a, String b -> -(a.size()) <=> -(b.size()) }.each { extension ->
            if (filename.endsWith(".${extension}")) {
                String potentialName = filename.substring(0, filename.lastIndexOf(".${extension}"))
                if (potentialName.length() < rootName.length()) {
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
        Class<AssetFile> fileSpec = assetForFileName(uri)
        if (fileSpec) {
            if (fileSpec.contentType instanceof String) {
                return [fileSpec.contentType]
            }
            return fileSpec.contentType
        }
        return null
    }

    /**
     *
     * @param uri string representation of the asset file.
     * @param ext the extension of the file
     * @return An instance of the file that the uri belongs to.
     */
    static AssetFile getAssetFileWithExtension(String uri, String ext) {
        String fullName = uri
        if (ext) {
            fullName = uri + "." + ext
        }
        AssetFile assetFile = AssetHelper.fileForFullName(fullName)
        if (assetFile) {
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
        return assetFileClasses().findAll { Class<AssetFile> it -> (it.contentType instanceof String) ? it.contentType == contentType : contentType in it.contentType }
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
        byte[] checksum = md.digest()
        return checksum.encodeHex().toString()
    }

    /**
     * Normalizes a path into a standard path, stripping out all path elements that walk the path (i.e. '..' and '.')
     * @param path String path (i.e. '/path/to/../file.js')
     * @return normalied path String (i.e. '/path/file.js')
     */
    static String normalizePath(String path) {
        String[] pathArgs = path.split("/")
        List newPath = []
        for (int counter = 0; counter < pathArgs.length; counter++) {
            String pathElement = pathArgs[counter]
            if (pathElement == '..') {
                if (newPath.size() > 0) {
                    newPath.pop()
                } else if (counter < pathArgs.length - 1) {
                    counter++
                    continue;
                }
            } else if (pathElement == '.') {
                // do nothing
            } else {
                newPath << pathElement
            }
        }
        return newPath.join("/")
    }

    /**
     * Checks if a file path matches any pattern provided. These default to glob format but can be changed to use
     * regular expressions by prefixing the pattern string with 'regex:'
     * @param filePath String the fully qualified asset path we are checking
     * @param patterns a List<String> of patterns either GLOB or regex (to use regex prefix the string with 'regex:')
     * @return boolean true/false depending on wether or not the file path matches any patterns
     */
    @CompileStatic
    static boolean isFileMatchingPatterns(String filePath, List<String> patterns) {
        for(pattern in patterns) {
            String syntax = "glob"
            if(pattern.startsWith('regex:')) {
                syntax = "regex"
                pattern = pattern.substring(6)
            } else if(pattern.startsWith('glob:')) {
                pattern = pattern.substring(5)
            }
            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("${syntax}:${pattern}")
            if(pathMatcher.matches(Paths.get(filePath))) {
                return true
            }
            if(syntax == "glob" && pattern.startsWith('**/')) {
                pathMatcher = FileSystems.getDefault().getPathMatcher("${syntax}:${pattern.substring(3)}")
                if(pathMatcher.matches(Paths.get(filePath))) {
                    return true
                }
            }
        }
        return false
    }

}

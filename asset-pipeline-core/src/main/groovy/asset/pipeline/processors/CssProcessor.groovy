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
package asset.pipeline.processors

import asset.pipeline.*
import java.net.URL
import java.net.URI
import groovy.transform.CompileStatic
/**
* This Processor iterates over relative image paths in a CSS file and
* recalculates their path relative to the base file. In precompiler mode
* the image urls are also cache digested.
* @author David Estes
*/
class CssProcessor extends AbstractProcessor {

    CssProcessor(AssetCompiler precompiler) {
        super(precompiler)
    }

    String process(String inputText, AssetFile assetFile) {
            Map cachedPaths = [:]
            return inputText.replaceAll(/url\((?:\s+)?[\'\"]?([a-zA-Z0-9\-\_\.\/\@\#\?\ \&\+\%\=]+)[\'\"]?(?:\s+)?\)/) { fullMatch, assetPath ->
                String replacementPath = assetPath.trim()
                if(cachedPaths[assetPath]) {
                    replacementPath = cachedPaths[assetPath].path
                } else if(replacementPath.size() > 0 && isRelativePath(replacementPath)) {
                    def urlRep = new URL("http://hostname/${replacementPath}") //Split out subcomponents
                    def relativeFileName = assetFile.parentPath ? [assetFile.parentPath,urlRep.path.substring(1)].join("/") : urlRep.path.substring(1)
                    def normalizedFileName = AssetHelper.normalizePath(relativeFileName)
                    def cssFile = null
        
                    if(!cssFile) {
                        cssFile = AssetHelper.fileForFullName(normalizedFileName)
                    }
                    if(cssFile) {
                        replacementPath = relativePathToBaseFile(cssFile, assetFile.baseFile ?: assetFile, this.precompiler && this.precompiler.options.enableDigests ? true : false)
                        if(urlRep.query != null) {
                            replacementPath += "?${urlRep.query}"
                        }
                        if(urlRep.ref) {
                            replacementPath += "#${urlRep.ref}"
                        }
                        cachedPaths[assetPath] = [path:replacementPath]
                    } else {
                        cachedPaths[assetPath] = [path:replacementPath]
                    }
                }
                return "url('${replacementPath}')"
            }
    }

    private isRelativePath(assetPath) {
        return !assetPath.startsWith("/") && !assetPath.startsWith("http")
    }

    private relativePathToBaseFile(file, baseFile, useDigest=false) {
        def baseRelativePath = baseFile.parentPath ? baseFile.parentPath.split(AssetHelper.DIRECTIVE_FILE_SEPARATOR).findAll{it}.reverse() : []
        def currentRelativePath = file.parentPath ? file.parentPath.split(AssetHelper.DIRECTIVE_FILE_SEPARATOR).findAll({it}).reverse() : []
        def filePathIndex=currentRelativePath.size()- 1
        def baseFileIndex=baseRelativePath.size() - 1

        while(filePathIndex > 0 && baseFileIndex > 0 && baseRelativePath[baseFileIndex] == currentRelativePath[filePathIndex]) {
            filePathIndex--
            baseFileIndex--
        }

        def calculatedPath = []

        // for each remaining level in the home path, add a ..
        for(;baseFileIndex>=0;baseFileIndex--) {
            calculatedPath << ".."
        }

        for(;filePathIndex>=0;filePathIndex--) {
            calculatedPath << currentRelativePath[filePathIndex]
        }
        if(useDigest) {
            def extension = AssetHelper.extensionFromURI(file.getName())
            def fileName  = AssetHelper.nameWithoutExtension(file.getName())
            def digestName
            if(!(file instanceof GenericAssetFile)) {
                extension = file.compiledExtension
                def directiveProcessor = new DirectiveProcessor(baseFile.contentType[0], precompiler)
                def fileData   = directiveProcessor.compile(file)
                digestName = AssetHelper.getByteDigest(fileData.bytes)
            }
            else {
                digestName = AssetHelper.getByteDigest(file.bytes)
            }
            calculatedPath << "${fileName}-${digestName}.${extension}"
        } else {
            if(!(file instanceof GenericAssetFile)) {
                def fileName  = AssetHelper.nameWithoutExtension(file.getName())
                def extension = file.compiledExtension
                calculatedPath << "${fileName}.${extension}"
            } else {
                calculatedPath << file.getName()
            }
        }

        return calculatedPath.join(AssetHelper.DIRECTIVE_FILE_SEPARATOR)
    }

}

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

import java.nio.charset.Charset


@Commons
class DirectiveProcessor {

    /**
    * References names of directives to implemented methods.
    * This can be adjusted at run time to either override behavior or add custom directives
    */
    static DIRECTIVES = [require_self: "requireSelfDirective" ,require_tree: "requireTreeDirective", require_full_tree: "requireFullTreeDirective" , require: "requireFileDirective", encoding: "encodingTypeDirective"]

    private String contentType
    private AssetCompiler precompiler
    private def files = []
    private def baseFile

    DirectiveProcessor(String contentType, AssetCompiler precompiler = null) {
        this.contentType = contentType
        this.precompiler = precompiler
    }


    /**
    * Takes an AssetFile and compiles it to a final result based on input directives and
    * setup Processors. If a GenericAssetFile is passed in the raw byte array is returned.
    * @param file an instance of an AbstractAssetFile (i.e. JsAssetFile or CssAssetFile)
    */
    def compile(AssetFile file) {
        if(file instanceof GenericAssetFile) {
            return file.getBytes()
        }
        this.baseFile = file
        this.files = []
        def tree = getDependencyTree(file)
        def buffer = ""

        buffer = loadContentsForTree(tree,buffer)
        return buffer
    }

    /**
    * Returns a Flattened list of files based on the require tree
    * This is useful for converting a script tag into several script tags for debugging
    * @param file an instance of an AbstractAssetFile (i.e. JsAssetFile or CssAssetFile)
    */
    def getFlattenedRequireList(file) {
        if(file instanceof GenericAssetFile) {
            return [path: file.path, encoding: null]
        }
        def flattenedList = []
        def tree = getDependencyTree(file)

        flattenedList = loadRequiresForTree(tree, flattenedList)
        return flattenedList
    }


    /**
    * Scans through a generated tree and builds a flatted list of requirements recursively
    */
    protected loadRequiresForTree(treeSet, flattenedList) {
        def selfLoaded = false
        for(childTree in treeSet.tree) {
            if(childTree == "self") {
                def extension = treeSet.file.compiledExtension
                def fileName = AssetHelper.fileNameWithoutExtensionFromArtefact(treeSet.file.path,treeSet.file)
                flattenedList << [path: "${fileName}.${extension}", encoding: treeSet.file.encoding]
                selfLoaded = true
            } else {
                flattenedList = loadRequiresForTree(childTree, flattenedList)
            }
        }

        if(!selfLoaded) {
            def extension = treeSet.file.compiledExtension
            def fileName = AssetHelper.fileNameWithoutExtensionFromArtefact(treeSet.file.path,treeSet.file)
            flattenedList << [path: "${fileName}.${extension}", encoding: treeSet.file.encoding]
        }
        return flattenedList
    }


    /**
    * Scans through a generated tree and builds a files contents recursively
    */
    protected loadContentsForTree(treeSet,buffer) {
        def selfLoaded = false
        for(childTree in treeSet.tree) {
            if(childTree == "self") {
                buffer += fileContents(treeSet.file) + "\n"
                selfLoaded = true
            } else {
                buffer = loadContentsForTree(childTree,buffer)
            }
        }

        if(!selfLoaded) {
            buffer += fileContents(treeSet.file) + "\n"
        }
        return buffer
    }

    /**
    * Builds a dependency tree for a particular file
    */
    protected getDependencyTree(file) {
        this.files << file
        def tree = [file:file,tree:[]]
        if(!(file instanceof GenericAssetFile)) {
            this.findDirectives(file,tree)
        }

        return tree
    }

    /**
    * Scans an AssetFile for directive patterns and builds a dependency tree
    * @param fileSpec The assetFile we wish to scan
    * @param tree The tree object we use to build the graph (should be a List)
    */
    protected findDirectives(fileSpec, tree) {
        def lines = fileSpec.inputStream.readLines()
        def startTime = new Date().time
        lines.find { line ->
            def directive = fileSpec.directiveForLine(line)
            if(directive) {
            	directive = directive.trim()
                def unprocessedArgs = directive.split(/\s+/)

                def processor = DIRECTIVES[unprocessedArgs[0].toLowerCase()]

                if(processor) {
                    def directiveArguments = unprocessedArgs
                    if(directive.indexOf('$') >= 0) {
                        directiveArguments = new groovy.text.GStringTemplateEngine(this.class.classLoader).createTemplate(directive).make().toString().split(/\s+/)
                    }
                    directiveArguments[0] = directiveArguments[0].toLowerCase()
                    this."${processor}"(directiveArguments, fileSpec,tree)
                }
            }
            return false
        }
    }


    /**
    * Used to control file order for when content within your manifest exists
    */
    def requireSelfDirective(command, file, tree) {
        tree.tree << "self"
    }

    /**
    * Set your file encoding within your manifest
    */
    def encodingTypeDirective(command, fileSpec, tree) {
        if(!command[1]) {
            return;
        }
        if(fileSpec.baseFile) {
           fileSpec.baseFile.encoding = command[1]
        }
        fileSpec.encoding = command[1]
    }


    /**
    * Loads files recursively within the specified folder within the same source resolver
    * Example: //=require_tree .
    */
    def requireTreeDirective(command, fileSpec, tree) {
        String directivePath = command[1]
        def resolver = fileSpec.sourceResolver
        def files = resolver.getAssets(directivePath,contentType,null,true ,fileSpec,baseFile)

        files.each { file ->
            if(!isFileInTree(file,tree)) {
                tree.tree << getDependencyTree(file)
            }
        }
    }


    /**
    * Loads files recursively within the specified relative path across ALL resolvers
    * Example: //=require_full_tree /spud/admin
    */
    def requireFullTreeDirective(command, fileSpec, tree) {
        String directivePath = command[1]
        for(resolver in AssetPipelineConfigHolder.resolvers) {
            def files = resolver.getAssets(directivePath,contentType,null,true ,fileSpec,baseFile)
            files.each { file ->
                if(!isFileInTree(file,tree)) {
                    tree.tree << getDependencyTree(file)
                }
            }
        }
    }

    /**
    * Directive which allows inclusion of individual files
    * Example: //=require sample.js
    */
    def requireFileDirective(command, file, tree) {
        def fileName = command[1]

        List fileNameList = fileName.tokenize(',')
        if( fileNameList.size() > 1 ) {
            fileNameList.each{ thisListItem ->
                requireFileDirective( [ command[0], thisListItem ], file, tree )
            }
        }
        else {
            def newFile
            if( fileName.startsWith( AssetHelper.DIRECTIVE_FILE_SEPARATOR ) ) {
                newFile = AssetHelper.fileForUri( fileName, this.contentType, null, this.baseFile )
            }
            else {
                def relativeFileName = [ file.parentPath, fileName ].join( AssetHelper.DIRECTIVE_FILE_SEPARATOR )
                newFile = AssetHelper.fileForUri( relativeFileName, this.contentType, null, this.baseFile )
            }

            if( newFile ) {
                if( !isFileInTree( newFile, tree ) ) {
                    tree.tree << getDependencyTree( newFile )
                }
            }
            else if( !fileName.startsWith( AssetHelper.DIRECTIVE_FILE_SEPARATOR ) ) {
                command[ 1 ] = AssetHelper.DIRECTIVE_FILE_SEPARATOR + command[ 1 ]
                requireFileDirective( command, file, tree )
            }
            else {
                log.warn( "Unable to Locate Asset: ${ command[ 1 ] }" )
            }
        }
    }

    protected boolean isFileInTree(file,currentTree) {
        def result = files.find { it ->
            it.path == file.path
        }
        if(result) {
            return true
        } else {
            return false
        }
    }

    /**
    * Used for fetching the contents of a file be it a Generic unprocessable entity
    * or an AssetFile with a processable stream
    */
    String fileContents(AssetFile file) {
        return file.processedStream(this.precompiler)
    }

}

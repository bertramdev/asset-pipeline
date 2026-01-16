/*
 * Copyright 2016 the original author or authors.
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
package asset.pipeline.jsass

import asset.pipeline.AssetFile
import groovy.util.logging.Slf4j

import java.util.regex.Pattern
import asset.pipeline.AssetHelper
import asset.pipeline.CacheManager
import io.bit3.jsass.importer.Import
import io.bit3.jsass.importer.Importer

import java.nio.file.Path
import java.nio.file.Paths

@Slf4j
class SassAssetFileImporter implements Importer {
    AssetFile baseFile
    static String QUOTED_FILE_SEPARATOR = Pattern.quote(File.separator)
    static String DIRECTIVE_FILE_SEPARATOR = '/'
    SassAssetFileImporter(AssetFile assetFile) {
        super()
        this.baseFile = assetFile
    }

    /**
     * Find the real file name to be resolved to a AssetFile instance
     * This method tries to resolve path/to/imported.scss and path/to/_imported.scss
     * TODO: Make sure there are no other ways to include files in SASS.
     * @param url
     * @return
     */
		AssetFile getAssetFromScssImport(String parent, String fileName) {
			def newFile
			if( fileName.startsWith( AssetHelper.DIRECTIVE_FILE_SEPARATOR ) ) {
				newFile = AssetHelper.fileForUri( getPartialPath(fileName) , 'text/css', null, baseFile )
				if(!newFile) {
					newFile = AssetHelper.fileForUri( fileName, 'text/css', null, baseFile )
				}
			} else
			{
				String parentPath = ""
				String[] pathArgs = parent.split("/")
				if(pathArgs.size() > 1) {
					parentPath = (Arrays.copyOfRange(pathArgs,0,pathArgs.size() - 1) as String[]).join("/")

				}
//				parentPath = AssetHelper.DIRECTIVE_FILE_SEPARATOR + parentPath

				def relativeFileName = [ parentPath, fileName ].join( AssetHelper.DIRECTIVE_FILE_SEPARATOR )

				//we should try mixins first
				newFile = AssetHelper.fileForUri( getPartialPath(relativeFileName), 'text/css', null, baseFile )

				if(!newFile) {
					newFile = AssetHelper.fileForUri( relativeFileName, 'text/css', null, baseFile )
				}
			}


			if( !newFile && !fileName.startsWith( AssetHelper.DIRECTIVE_FILE_SEPARATOR ) ) {
				newFile = AssetHelper.fileForUri( getPartialPath(AssetHelper.DIRECTIVE_FILE_SEPARATOR + fileName), 'text/css', null, baseFile )
				if(!newFile) {
					newFile = AssetHelper.fileForUri( AssetHelper.DIRECTIVE_FILE_SEPARATOR + fileName, 'text/css', null, baseFile )
				}
			}

			if (!newFile) {
				log.warn( "Unable to Locate Asset: ${ fileName }" )
			}

			if(newFile) {
//				CacheManager.addCacheDependency(baseFile?.path ?: parent, newFile)
				return newFile
			}


			return null
    }

    @Override
    public Collection<Import> apply(String url, Import previous) {
        def importedAssetFile = getAssetFromScssImport(previous.absoluteUri.toString(), url)

        if (baseFile && importedAssetFile) {
            CacheManager.addCacheDependency(baseFile.path, importedAssetFile)
            def results = Collections.singletonList(
                    new Import(importedAssetFile.name, importedAssetFile.path, importedAssetFile.inputStream.getText('UTF-8'))
                    )
            return results
        }

        // at this point, compilation will fail
        return null;
    }

		private String getPartialPath(String originalUri) {
			String[] components = originalUri.split(DIRECTIVE_FILE_SEPARATOR);
			String fileName = components[components.length-1]
			components[components.length-1] = "_" + fileName
			return components.join(DIRECTIVE_FILE_SEPARATOR)
		}
}

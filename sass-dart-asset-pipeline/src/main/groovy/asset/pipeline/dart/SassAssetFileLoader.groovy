package asset.pipeline.dart

import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import asset.pipeline.CacheManager
import com.caoccao.javet.annotations.V8Function
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.util.regex.Pattern

@Slf4j
@CompileStatic
class SassAssetFileLoader {
    static String QUOTED_FILE_SEPARATOR = Pattern.quote(File.separator)
    static String DIRECTIVE_FILE_SEPARATOR = '/'

    AssetFile baseFile

    SassAssetFileLoader(AssetFile assetFile) {
        this.baseFile = assetFile
    }

    /**
     * Java callback function for the dart-sass modern Importer API.
     * Called from the JS canonicalize/load importer wrapper.
     * https://sass-lang.com/documentation/js-api/interfaces/importer/
     *
     * @param url the import as it appears in the source file
     * @param containingPath the resolved path of the parent file, or 'stdin' for the top-level file
     * @return a map with 'contents' (file contents) and 'path' (resolved canonical path)
     */
    @V8Function
    @SuppressWarnings('unused')
    Map resolveImport(String url, String containingPath) {
        log.debug("Importing for url [{}], containingPath [{}], base file [{}]", url, containingPath, baseFile?.path)

        // The initial import has a containingPath of 'stdin', use the base file path instead
        if (containingPath == 'stdin') {
            containingPath = baseFile.path
        }

        AssetFile imported = getAssetFromScssImport(containingPath, url)
        CacheManager.addCacheDependency(baseFile.path, imported)

        return [contents: imported.inputStream.text, path: imported.path]
    }

    /**
     * Find the real file name to be resolved to a AssetFile instance
     * This method tries to resolve path/to/imported.scss and path/to/_imported.scss
     *
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
        }
        else 
        {
            String parentPath = ""
            String[] pathArgs = parent.split("/")
            if(pathArgs.size() > 1) {
                parentPath = (Arrays.copyOfRange(pathArgs,0,pathArgs.size() - 1) as String[]).join("/")
            }
            
            def relativeFileName = [ parentPath, fileName ].join( AssetHelper.DIRECTIVE_FILE_SEPARATOR )
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
        else if (!newFile) {
            log.warn( "Unable to Locate Asset: ${ fileName }" )
        }

        if(newFile) {
            CacheManager.addCacheDependency(baseFile?.path ?: parent, newFile)
            return newFile
        }


        return null
    }

    private String getPartialPath(String originalUri) {
        String[] components = originalUri.split(DIRECTIVE_FILE_SEPARATOR);
        String fileName = components[components.length-1]
        components[components.length-1] = "_" + fileName
        return components.join(DIRECTIVE_FILE_SEPARATOR)
    }
}

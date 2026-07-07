package asset.pipeline.dart

import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import asset.pipeline.CacheManager
import com.caoccao.javet.annotations.V8Function
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

@Slf4j
@CompileStatic
class SassAssetFileLoader {
    static String QUOTED_FILE_SEPARATOR = Pattern.quote(File.separator)
    static String DIRECTIVE_FILE_SEPARATOR = '/'

    AssetFile baseFile

    Map<String, String> importMap = [:]
    Map<String, String> resolvedPaths = [:]

    SassAssetFileLoader(AssetFile assetFile) {
        this.baseFile = assetFile
    }

    /**
     * Java callback function for the dart-sass Importer API
     * https://sass-lang.com/documentation/js-api/interfaces/LegacySharedOptions#importer
     *
     * @param url the import it appears in the source file
     * @prev either 'stdin' for the first level imports or the original url from the parent for nested
     * @return https://sass-lang.com/documentation/js-api/modules#LegacyImporterResult
     */
    @V8Function
    @SuppressWarnings('unused')
    Map resolveImport(String url, String prev) {
        log.debug("Importing for url [{}], prev [{}], base file [{}]", url, prev, baseFile?.path)

        // The initial import has a path of stdin, but we need to convert that to the proper base path
        // Otherwise, if we have a parent, append that to form the correct URL as the importer syntax doesn't send what's expected
        if (prev == 'stdin') {
            prev = baseFile.path
        }
        else {
            // Resolve the real base path for this import if it's not an absolute path
            String priorParent = importMap[prev]
            if (priorParent && !prev.startsWith('/')) {
                Path priorParentPath = Paths.get(priorParent)
                if (priorParentPath.parent != null) {
                    prev = "${priorParentPath.parent.toString()}/${prev}"
                }
            }
        }

        // For each URL remember the last prev, this allows us to resolve nested imports since dart doesn't
        // give us the full path when using stdin
        importMap[url] = prev

        AssetFile imported = getAssetFromScssImport(prev, url)
        CacheManager.addCacheDependency(baseFile.path, imported)

        return [contents: imported.inputStream.text]
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

package asset.pipeline.dart

import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
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

    SassAssetFileLoader(AssetFile assetFile) {
        this.baseFile = assetFile
    }

    @V8Function
    @SuppressWarnings('unused')
    Map resolveImport(String url, String prev, String assetFilePath) {
        println "Import - Url=$url, Prev=$prev, AssetFilePath=$assetFilePath"

        Path filePath = Paths.get(assetFilePath)
        if (prev == 'stdin') {
            prev = assetFilePath
        }
        else if (filePath.parent) {
            prev = "${filePath.parent.toString()}/${prev}"
        }

        AssetFile imported = getAssetFromScssImport(prev, url)
        [contents: imported.inputStream.text]
    }

    /**
     * Find the real file name to be resolved to a AssetFile instance
     * This method tries to resolve path/to/imported.scss and path/to/_imported.scss
     * @param url
     * @return
     */
    AssetFile getAssetFromScssImport(String parent, String importUrl) {
        Path parentPath = Paths.get(parent)
        Path relativeRootPath = parentPath.parent ?: Paths.get('.')
        Path importUrlPath = Paths.get(importUrl)

        List<String> possibleStylesheets = SassAssetFile.extensions.collectMany { String extension ->
            [
                relativeRootPath.resolve("${importUrlPath}.${extension}").toString(),
                relativeRootPath.resolve("${importUrlPath.parent ? importUrlPath.parent.toString() + '/' : ''}_${importUrlPath.fileName}.${extension}").toString(),
                "${importUrlPath.fileName}.${extension}",
                "_${importUrlPath.fileName}.${extension}"
            ] as List<String>
        }

        for (String stylesheetPath : possibleStylesheets) {
            String standardPathStyle = stylesheetPath?.replaceAll(QUOTED_FILE_SEPARATOR, DIRECTIVE_FILE_SEPARATOR)
            AssetFile assetFile = AssetHelper.fileForFullName(standardPathStyle.toString())
            if (assetFile) {
                log.debug "$parent imported $assetFile.path"
                return assetFile
            }
        }

        log.error "Unable to find the asset for $importUrl imported by $parent"
        return null
    }
}

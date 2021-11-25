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

    Map<String, String> importMap = [:]

    SassAssetFileLoader(AssetFile assetFile) {
        this.baseFile = assetFile
    }

    /**
     * Java callback function for the dart-sass Importer API
     * https://sass-lang.com/documentation/js-api/interfaces/LegacySharedOptions#importer
     *
     * @param url the import it appears in the source file
     * @prev either 'stdin' for the first level imports or the original url from the parent for nested
     * @param assetFilePath the original assetFile.path that started the import chain
     * @return https://sass-lang.com/documentation/js-api/modules#LegacyImporterResult
     */
    @V8Function
    @SuppressWarnings('unused')
    Map resolveImport(String url, String prev, String assetFilePath) {
        log.debug("resolving import for url [{}], prev [{}], asset file path [{}]", url, prev, assetFilePath)
        println "    > Importing [${url}], prev [$prev], asset file [${assetFilePath}]"

        // The initial import has a path of stdin, but we need to convert that to the proper base path
        // Otherwise, if we have a parent, append that to form the correct URL as the importer syntax doesn't send what's expected
        if (prev == 'stdin') {
            prev = assetFilePath
        }
        else {
            // Resolve the real base path for this import
            String priorParent = importMap.find { it.value == prev }?.key
            if (priorParent) {
                Path priorParentPath = Paths.get(priorParent)
                if (priorParentPath.parent) {
                    prev = "${priorParentPath.parent.toString()}/${prev}"
                }
            }
        }

        importMap[prev] = url

        AssetFile imported = getAssetFromScssImport(prev, url)
        return [contents: imported.inputStream.text]
    }

    /**
     * Find the real file name to be resolved to a AssetFile instance
     * This method tries to resolve path/to/imported.scss and path/to/_imported.scss
     *
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

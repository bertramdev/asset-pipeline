package asset.pipeline.processors


import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.GenericAssetFile

import static asset.pipeline.AssetHelper.extensionFromURI
import static asset.pipeline.AssetHelper.fileForFullName
import static asset.pipeline.AssetHelper.getByteDigest
import static asset.pipeline.AssetHelper.nameWithoutExtension
import static asset.pipeline.AssetHelper.normalizePath
import static asset.pipeline.utils.net.Urls.getSchemeWithColon


/**
 * @author Ross Goldberg
 * @author David Estes
 */
abstract class AbstractUrlRewritingProcessor extends AbstractProcessor {

    /**
     * Constructor for building a Processor
     *
     * @param precompiler - An Instance of the AssetCompiler class compiling the file or NULL for dev mode.
     */
    AbstractUrlRewritingProcessor(final AssetCompiler precompiler) {
        super(precompiler)
    }


    protected String replacementUrl(final AssetFile assetFile, final String url) {
        final String schemeWithColon = getSchemeWithColon(url)

        final String urlSansScheme =
            schemeWithColon \
                ? url.substring(schemeWithColon.length())
                : url

        final URL       urlSplitter = new URL('http', 'hostname', urlSansScheme)
        final String    parentPath  = assetFile.parentPath
        final AssetFile currFile    =
            fileForFullName(
                normalizePath(
                    parentPath
                        ? parentPath + '/' + urlSplitter.path
                        : urlSplitter.path
                )
            )

        if (! currFile) {
            return null
        }

        final StringBuilder replacementPathSb = new StringBuilder()

        // scheme (aka protocol)
        if (schemeWithColon) {
            replacementPathSb << schemeWithColon
        }

        // relative parent path
        final String baseFileParentPath = assetFile.baseFile?.parentPath ?: assetFile.parentPath
        final String currFileParentPath = currFile.parentPath

        final List<String> baseRelativePath = baseFileParentPath ? baseFileParentPath.split('/').findAll {it}.reverse() : []
        final List<String> currRelativePath = currFileParentPath ? currFileParentPath.split('/').findAll {it}.reverse() : []

        int baseIndex = baseRelativePath.size() - 1
        int currIndex = currRelativePath.size() - 1

        while (baseIndex > 0 && currIndex > 0 && baseRelativePath[baseIndex] == currRelativePath[currIndex]) {
            baseIndex--
            currIndex--
        }

        for (; baseIndex >= 0; baseIndex--) { // for each remaining level in the base path, add a ..
            replacementPathSb << '../'
        }

        for (; currIndex >= 0; currIndex--) {
            replacementPathSb << currRelativePath[currIndex] << ('/' as char)
        }

        // file
        final Map     options       = precompiler?.options
        final boolean enableDigests =
            options \
                ? options.containsKey('enableDigests') \
                    ? options.enableDigests
                    : true
                : true

        replacementPathSb << (
            enableDigests \
                ? currFile instanceof GenericAssetFile
                    ? "${nameWithoutExtension(currFile.name)}-${getByteDigest(currFile.bytes)}.${extensionFromURI(currFile.name)}"
                    : digestedNonGenericAssetFileName(currFile, nameWithoutExtension(currFile.name))
                : currFile instanceof GenericAssetFile
                    ? currFile.name
                    : nameWithoutExtension(currFile.name) + '.' + currFile.compiledExtension
        )

        // query
        if (urlSplitter.query != null) {
            replacementPathSb << '?' << urlSplitter.query
        }

        // fragment (aka reference; aka anchor)
        if (urlSplitter.ref) {
            replacementPathSb << '#' << urlSplitter.ref
        }

        return replacementPathSb.toString()
    }


    protected abstract String digestedNonGenericAssetFileName(AssetFile assetFile, String fileNameSansExt)
}

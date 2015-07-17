package asset.pipeline.processors


import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.GenericAssetFile

import static asset.pipeline.AssetHelper.DIRECTIVE_FILE_SEPARATOR
import static asset.pipeline.AssetHelper.extensionFromURI
import static asset.pipeline.AssetHelper.fileForFullName
import static asset.pipeline.AssetHelper.getByteDigest
import static asset.pipeline.AssetHelper.nameWithoutExtension
import static asset.pipeline.AssetHelper.normalizePath


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
        final URL urlSplitter = new URL("http://hostname/${url}")

        final AssetFile currFile =
            fileForFullName(
                normalizePath(
                    assetFile.parentPath
                        ? assetFile.parentPath + urlSplitter.path
                        : urlSplitter.path.substring(1)
                )
            )

        if (! currFile) {
            return null
        }

        final AssetFile baseFile = assetFile.baseFile ?: assetFile

        final List<String> baseRelativePath = baseFile.parentPath ? baseFile.parentPath.split(DIRECTIVE_FILE_SEPARATOR).findAll {it}.reverse() : []
        final List<String> currRelativePath = currFile.parentPath ? currFile.parentPath.split(DIRECTIVE_FILE_SEPARATOR).findAll {it}.reverse() : []

        int baseIndex = baseRelativePath.size() - 1
        int currIndex = currRelativePath.size() - 1

        while (baseIndex > 0 && currIndex > 0 && baseRelativePath[baseIndex] == currRelativePath[currIndex]) {
            baseIndex--
            currIndex--
        }

        final StringBuilder replacementPathSb = new StringBuilder()

        // for each remaining level in the base path, add a ..
        for (; baseIndex >= 0; baseIndex--) {
            replacementPathSb.append('../')
        }

        for (; currIndex >= 0; currIndex--) {
            replacementPathSb.append(currRelativePath[currIndex])
            replacementPathSb.append('/' as char)
        }

        final String fileName = nameWithoutExtension(currFile.name)
        replacementPathSb.append(
            precompiler?.options.enableDigests
                ? currFile instanceof GenericAssetFile
                    ? "${fileName}-${getByteDigest(currFile.bytes)}.${extensionFromURI(currFile.name)}"
                    : digestedNonGenericAssetFileName(currFile, baseFile, fileName)
                : currFile instanceof GenericAssetFile
                    ? currFile.name
                    : fileName + '.' + currFile.compiledExtension
        )

        if (urlSplitter.query != null) {
            replacementPathSb.append('?').append(urlSplitter.query)
        }

        if (urlSplitter.ref) {
            replacementPathSb.append('#').append(urlSplitter.ref)
        }

        return replacementPathSb.toString()
    }


    protected abstract String digestedNonGenericAssetFileName(AssetFile file, AssetFile baseFile, String fileName)
}

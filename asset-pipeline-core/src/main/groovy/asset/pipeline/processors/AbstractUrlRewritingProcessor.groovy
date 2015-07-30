package asset.pipeline.processors


import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.DirectiveProcessor
import asset.pipeline.GenericAssetFile

import static asset.pipeline.AssetHelper.DIRECTIVE_FILE_SEPARATOR
import static asset.pipeline.AssetHelper.extensionFromURI
import static asset.pipeline.AssetHelper.getByteDigest
import static asset.pipeline.AssetHelper.nameWithoutExtension


/**
 * Subclasses iterate over relative asset URLs in a base asset file and
 * rewrites the URL relative to the base asset file.
 *
 * In precompiler mode, the asset URLs are also cache digested.
 *
 * @author David Estes
 * @author Ross Goldberg
 */
abstract class AbstractUrlRewritingProcessor extends AbstractProcessor {

    AbstractUrlRewritingProcessor(final AssetCompiler precompiler) {
        super(precompiler)
    }


    protected String relativePathToBaseFile(final AssetFile currFile, final AssetFile baseFile, final boolean useDigest = false) {
        final String baseFileParentPath = baseFile.parentPath
        final String currFileParentPath = currFile.parentPath

        final List<String> baseRelativePath = baseFileParentPath ? baseFileParentPath.split(DIRECTIVE_FILE_SEPARATOR).findAll {it}.reverse() : []
        final List<String> currRelativePath = currFileParentPath ? currFileParentPath.split(DIRECTIVE_FILE_SEPARATOR).findAll {it}.reverse() : []

        int basePathIndex = baseRelativePath.size() - 1
        int currPathIndex = currRelativePath.size() - 1

        while (basePathIndex >= 0 && currPathIndex >= 0 && baseRelativePath[basePathIndex] == currRelativePath[currPathIndex]) {
            basePathIndex--
            currPathIndex--
        }

        final List<String> calculatedPath = new ArrayList<>(basePathIndex + currPathIndex + 3)

        // for each remaining level in the base path, add a ..
        for (; basePathIndex >= 0; basePathIndex--) {
            calculatedPath << '..'
        }

        for (; currPathIndex >= 0; currPathIndex--) {
            calculatedPath << currRelativePath[currPathIndex]
        }

        final String fileName = nameWithoutExtension(currFile.name)
        if(useDigest) {
            if(currFile instanceof GenericAssetFile) {
                calculatedPath << "${fileName}-${getByteDigest(currFile.bytes)}.${extensionFromURI(currFile.name)}"
            } else {
                calculatedPath << "${fileName}-${getByteDigest(new DirectiveProcessor(currFile.contentType[0], precompiler).compile(currFile).bytes)}.${currFile.compiledExtension}"
            }
        } else {
            if(currFile instanceof GenericAssetFile) {
                calculatedPath << currFile.name
            } else {
                calculatedPath << "${fileName}.${currFile.compiledExtension}"
            }
        }

        return calculatedPath.join(DIRECTIVE_FILE_SEPARATOR)
    }
}

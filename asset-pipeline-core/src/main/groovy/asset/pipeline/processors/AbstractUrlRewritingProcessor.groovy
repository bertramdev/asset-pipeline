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


    protected String relativePathToBaseFile(final AssetFile file, final AssetFile baseFile, final boolean useDigest = false) {
        final List<String> baseRelativePath = baseFile.parentPath ? baseFile.parentPath.split(DIRECTIVE_FILE_SEPARATOR).findAll {it}.reverse() : []
        final List<String> currRelativePath =     file.parentPath ?     file.parentPath.split(DIRECTIVE_FILE_SEPARATOR).findAll {it}.reverse() : []

        int filePathIndex = currRelativePath.size() - 1
        int baseFileIndex = baseRelativePath.size() - 1

        while (filePathIndex >= 0 && baseFileIndex >= 0 && baseRelativePath[baseFileIndex] == currRelativePath[filePathIndex]) {
            filePathIndex--
            baseFileIndex--
        }

        final List<String> calculatedPath = new ArrayList<>(baseFileIndex + filePathIndex + 3)

        // for each remaining level in the home path, add a ..
        for (; baseFileIndex >= 0; baseFileIndex--) {
            calculatedPath << '..'
        }

        for (; filePathIndex >= 0; filePathIndex--) {
            calculatedPath << currRelativePath[filePathIndex]
        }

        final String fileName = nameWithoutExtension(file.name)
        if(useDigest) {
            if(file instanceof GenericAssetFile) {
                calculatedPath << "${fileName}-${getByteDigest(file.bytes)}.${extensionFromURI(file.name)}"
            } else {
                calculatedPath << "${fileName}-${getByteDigest(new DirectiveProcessor(file.contentType[0], precompiler).compile(file).bytes)}.${file.compiledExtension}"
            }
        } else {
            if(file instanceof GenericAssetFile) {
                calculatedPath << file.name
            } else {
                calculatedPath << "${fileName}.${file.compiledExtension}"
            }
        }

        return calculatedPath.join(DIRECTIVE_FILE_SEPARATOR)
    }
}

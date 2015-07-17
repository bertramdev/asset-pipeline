package asset.pipeline.processors


import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import asset.pipeline.GenericAssetFile


/**
 * @author Ross Goldberg
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


    protected String relativePathFromBaseFile(final AssetFile file, final AssetFile baseFile, final boolean useDigest = false) {
        final List<String> baseRelativePath = baseFile.parentPath ? baseFile.parentPath.split(AssetHelper.DIRECTIVE_FILE_SEPARATOR).findAll {it}.reverse() : []
        final List<String> currRelativePath =     file.parentPath ?     file.parentPath.split(AssetHelper.DIRECTIVE_FILE_SEPARATOR).findAll {it}.reverse() : []

        int filePathIndex = currRelativePath.size() - 1
        int baseFileIndex = baseRelativePath.size() - 1

        while (filePathIndex > 0 && baseFileIndex > 0 && baseRelativePath[baseFileIndex] == currRelativePath[filePathIndex]) {
            filePathIndex--
            baseFileIndex--
        }

        final List<String> calculatedPath = new ArrayList<>(baseFileIndex + filePathIndex + 3)

        // for each remaining level in the base path, add a ..
        for (; baseFileIndex >= 0; baseFileIndex--) {
            calculatedPath << '..'
        }

        for (; filePathIndex >= 0; filePathIndex--) {
            calculatedPath << currRelativePath[filePathIndex]
        }

        final String fileName = AssetHelper.nameWithoutExtension(file.name)
        calculatedPath << (
            useDigest
                ? file instanceof GenericAssetFile
                    ? "${fileName}-${AssetHelper.getByteDigest(file.bytes)}.${AssetHelper.extensionFromURI(file.name)}"
                    : digestedNonGenericAssetFileName()
                : file instanceof GenericAssetFile
                    ? file.name
                    : fileName + '.' + file.compiledExtension
        )

        return calculatedPath.join(AssetHelper.DIRECTIVE_FILE_SEPARATOR)
    }


    protected abstract String digestedNonGenericAssetFileName(AssetFile file, AssetFile baseFile, String fileName)
}

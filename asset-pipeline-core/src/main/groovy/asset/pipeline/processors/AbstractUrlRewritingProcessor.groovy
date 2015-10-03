package asset.pipeline.processors


import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.DirectiveProcessor
import asset.pipeline.GenericAssetFile

import static asset.pipeline.AssetHelper.DIRECTIVE_FILE_SEPARATOR
import static asset.pipeline.AssetHelper.extensionFromURI
import static asset.pipeline.AssetHelper.fileForFullName
import static asset.pipeline.AssetHelper.getByteDigest
import static asset.pipeline.AssetHelper.nameWithoutExtension
import static asset.pipeline.AssetHelper.normalizePath
import static asset.pipeline.utils.net.Urls.getSchemeWithColon


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

    private static final Set<String> NO_CACHE_DIGEST_FOR_COMPILED_EXTENSION_SET = []


    protected static boolean doNotInsertCacheDigestIntoUrlForCompiledExtension(final String compiledExtension) {
        NO_CACHE_DIGEST_FOR_COMPILED_EXTENSION_SET.add(compiledExtension)
    }


    AbstractUrlRewritingProcessor(final AssetCompiler precompiler) {
        super(precompiler)
    }


    protected String replacementUrl(final AssetFile assetFile, final String url) {
        final String schemeWithColon = getSchemeWithColon(url)

        final String urlSansScheme =
            schemeWithColon \
                ? url.substring(schemeWithColon.length())
                : url

        final URL urlSplitter = new URL('http', 'hostname', urlSansScheme)

        final AssetFile baseFile = assetFile.baseFile ?: assetFile
        final AssetFile currFile =
            fileForFullName(
                normalizePath(
                    assetFile.parentPath
                        ? assetFile.parentPath + DIRECTIVE_FILE_SEPARATOR + urlSplitter.path
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

        // for each remaining level in the base path, add a ..
        for (; basePathIndex >= 0; basePathIndex--) {
            replacementPathSb << '..' << DIRECTIVE_FILE_SEPARATOR
        }

        for (; currPathIndex >= 0; currPathIndex--) {
            replacementPathSb << currRelativePath[currPathIndex] << DIRECTIVE_FILE_SEPARATOR
        }

        // file
        final String fileName = nameWithoutExtension(currFile.name)
        if(precompiler && precompiler.options.enableDigests) {
            if(currFile instanceof GenericAssetFile) {
                replacementPathSb << fileName << '-' << currFile.getByteDigest() << '.' << extensionFromURI(currFile.name)
            } else {
                final String compiledExtension = currFile.compiledExtension
                if (NO_CACHE_DIGEST_FOR_COMPILED_EXTENSION_SET.contains(compiledExtension)) {
                    replacementPathSb << fileName << '.' << compiledExtension
                } else {
                    replacementPathSb << fileName << '-' << getByteDigest(new DirectiveProcessor(currFile.contentType[0], precompiler).compile(currFile).bytes) << '.' << compiledExtension
                }
            }
        } else {
            if(currFile instanceof GenericAssetFile) {
                replacementPathSb << currFile.name
            } else {
                replacementPathSb << fileName << '.' << currFile.compiledExtension
            }
        }

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
}

/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package asset.pipeline.processors


import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import java.util.regex.Pattern

import static asset.pipeline.utils.net.Urls.isRelative


/**
 * This Processor iterates over relative image paths in an HTML file and
 * recalculates their path relative to the base file. In precompiler mode
 * the image URLs are also cache digested.
 *
 * @author David Estes
 * @author Ross Goldberg
 */
class HtmlProcessor extends AbstractUrlRewritingProcessor {

    private static final Pattern QUOTED_ASSET_PATH_PATTERN = ~/"([a-zA-Z0-9\-_.\/@#? &+%=']++)"|'([a-zA-Z0-9\-_.\/@#? &+%="]++)'/


    HtmlProcessor(final AssetCompiler precompiler) {
        super(precompiler)
    }


    String process(final String inputText, final AssetFile assetFile) {
        final Map<String, String> cachedPaths = [:]
        return \
            inputText.replaceAll(QUOTED_ASSET_PATH_PATTERN) {
                final String quotedAssetPathWithQuotes,
                final String doubleQuotedAssetPath,
                final String singleQuotedAssetPath
            ->
                final String assetPath   = doubleQuotedAssetPath ?: singleQuotedAssetPath
                final String trimmedPath = assetPath.trim()

                final String replacementPath
                if (cachedPaths.containsKey(trimmedPath)) {
                    // cachedPaths[trimmedPath] == null // means use the incoming assetPath to preserve trim spacing
                    replacementPath = cachedPaths[trimmedPath] ?: assetPath
                }
                else if (trimmedPath.size() > 0 && isRelative(trimmedPath)) {
                    final URL       url              = new URL("http://hostname/${trimmedPath}") // Split out subcomponents
                    final String    relativeFileName = assetFile.parentPath ? assetFile.parentPath + url.path : url.path.substring(1)
                    final AssetFile file             = AssetHelper.fileForFullName(AssetHelper.normalizePath(relativeFileName))

                    if (file) {
                        final StringBuilder replacementPathSb = new StringBuilder()
                        replacementPathSb.append(relativePathToBaseFile(file, assetFile.baseFile ?: assetFile, precompiler && precompiler.options.enableDigests))
                        if (url.query != null) {
                            replacementPathSb.append('?').append(url.query)
                        }
                        if (url.ref) {
                            replacementPathSb.append('#').append(url.ref)
                        }
                        replacementPath          = replacementPathSb.toString()
                        cachedPaths[trimmedPath] = replacementPath
                    }
                    else {
                        cachedPaths[trimmedPath] = null
                        return quotedAssetPathWithQuotes
                    }
                }
                else {
                    //TODO? cachedPaths[trimmedPath] = null
                    return quotedAssetPathWithQuotes
                }

                final String quote = doubleQuotedAssetPath ? '"' : /'/
                return "${quote}${replacementPath}${quote}"
            }
    }
}

/*
 * I18nAssetFile.groovy
 *
 * Copyright (c) 2014-2016, Daniel Ellermann
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


package asset.pipeline.i18n

import asset.pipeline.AbstractAssetFile
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetHelper
import asset.pipeline.CacheManager
import asset.pipeline.Processor
import groovy.transform.CompileStatic
import java.util.regex.Pattern


/**
 * The class {@code I18nAssetFile} represents an asset file which converts code
 * keys to localized messages.
 *
 * @author  Daniel Ellermann
 * @author  David Estes
 * @version 3.0
 */
@CompileStatic
class I18nAssetFile extends AbstractAssetFile {

    //-- Class fields ---------------------------

    static final String compiledExtension = 'js'
    static final List<String> contentType = [
        'application/javascript', 'application/x-javascript', 'text/javascript'
    ]
    static List<String> extensions = ['i18n']
    static List<Class<Processor>> processors = [I18nProcessor]


    //-- Fields ---------------------------------

    Pattern directivePattern = ~/(?m)#=(.*)/


    //-- Public methods -------------------------

    @Override
    String processedStream(AssetCompiler precompiler) {
        // def skipCache = precompiler ?: (!processors || processors.size() == 0)

        String fileText = I18nPreprocessor.instance.preprocess(this)

        // def md5 = AssetHelper.getByteDigest(fileText.bytes)
        // if (!skipCache) {
        //     def cache = CacheManager.findCache(path, md5, baseFile?.path)
        //     if (cache) {
        //         return cache
        //     }
        // }

        for (processor in processors) {
            def processInstance = processor.newInstance(precompiler)
            fileText = processInstance.process(fileText, this)
        }

        // if (!skipCache) {
        //     CacheManager.createCache(path, md5, fileText, baseFile?.path)
        // }

        fileText
    }
}

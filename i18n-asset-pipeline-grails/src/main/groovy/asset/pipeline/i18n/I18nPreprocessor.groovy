/*
 * I18nPreprocessor.groovy
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

import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import groovy.transform.TypeChecked
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * The class {@code I18nPreprocessor} represents a pre-processor for i18n files
 * which are used in the asset pipeline.
 * <p>
 * Implementation notes: this class must be compiled with {@code @TypeChecked}
 * not {@code CompileStatic} because the unit test {@code I18nPreprocessorSpec}
 * mocks the static method {@code AssetFile.fileForUri} with meta class.
 *
 * @author  Daniel Ellermann
 * @author  David Estes
 * @version 3.0
 */
@TypeChecked
class I18nPreprocessor {

    //-- Constants ------------------------------

    protected static final Pattern REGEX_IGNORE = ~/^\s*(?:#.*)?$/
    protected static final Pattern REGEX_IMPORT = ~/^\s*@import\s+(.+)$/
    static final Pattern REGEX_FILENAME_SPLIT = ~/(\w+?)(_\w+)?\.i18n$/
    protected static final String EXTENSION = '.i18n'
    //-- Constructors ---------------------------

    protected I18nPreprocessor() {}


    //-- Public methods -------------------------

    /**
     * Gets the one and only factory instance.
     *
     * @return  the singleton instance of this factory
     */
    static I18nPreprocessor getInstance() {
        InstanceHolder.INSTANCE
    }

    /**
     * Pre-processes the given i18n file by removing empty lines and comment
     * lines and resolving all imports.
     *
     * @param file  the given i18n file
     * @param input the content of the i18n file
     * @return      the pre-processed content
     */
    String preprocess(AssetFile file) {
        StringBuilder sb = new StringBuilder()
        
        Set<String> resultCodes = new HashSet<>()
        
        String filename = file.name
        if (!filename.endsWith(EXTENSION)) {
            filename += EXTENSION
        }

        Matcher matcher = REGEX_FILENAME_SPLIT.matcher(filename)
        matcher.matches()

        String baseFilename = matcher.group(1)
        String locales = matcher.group(2)

        if(locales){
            String[] localeParts = locales?.split('_')

            String localeFile = baseFilename
            for(int i=0;i<localeParts.length;i++){
                if(i!=0) localeFile = localeFile + '_' + localeParts[i]
                if(i==localeParts.length-1){
                    doPreprocess(file,resultCodes)
                }
                else{
                    doPreprocess(AssetHelper.fileForUri(localeFile+EXTENSION),resultCodes)
                }
            }
        }
        else{
            doPreprocess(file,resultCodes)
        }
        
        resultCodes.inject(sb){acc,item->
            acc.append(item).append('\n')
        }.toString()
    }

    private void doPreprocess(AssetFile file,Set<String> codes){
        if(file==null) return
        
        def fileContent
        String encoding = file.baseFile?.encoding?:file.encoding
        if(encoding){
            fileContent = file.inputStream.getText(encoding)
        }
        else{
            fileContent = file.inputStream.text
        }

        doPreprocess(fileContent,codes)        
    }
    

    //-- Non-public methods ---------------------

    /**
     * Pre-processes an i18n file by removing empty lines and comment lines and
     * resolving all imports.
     *
     * @param input         the content of the i18n file
     * @param fileHistory   the history of all import files that have been
     *                      processed already; this is needed to handle
     *                      circular dependencies
     * @return              the pre-processed content
     */
    private void doPreprocess(String input, Set<String> codes) {
        input.eachLine { String line ->
            line = line.trim()
            if (line ==~ REGEX_IGNORE) return
            
            codes.add(line)
        }
    }

    //-- Inner classes --------------------------

    private static class InstanceHolder {

        //-- Constants --------------------------

        public static final I18nPreprocessor INSTANCE =
            new I18nPreprocessor()
    }
}

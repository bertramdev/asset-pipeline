/*
 * I18nTagLib.groovy
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

import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import asset.pipeline.grails.AssetProcessorService
import grails.artefact.TagLibrary
import grails.core.GrailsApplication
import groovy.transform.CompileStatic
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource


/**
 * Class {@code I18nTagLib} contains tags that help loading client-side i18n
 * files.
 *
 * @author  Daniel Ellermann
 * @version 3.0
 */
class I18nTagLib implements TagLibrary {

    //-- Class fields ---------------------------

    static namespace = 'asset'


    //-- Fields ---------------------------------

    AssetProcessorService assetProcessorService


    //-- Public methods -------------------------

    /**
     * Includes a JavaScript asset that provides client-side i18n for the given
     * locale.
     *
     * @attr locale the given locale
     * @attr [name] the name of the i18n file without extension; defaults to "messages"
     */
    def i18n = { attrs ->
        Properties manifest = AssetPipelineConfigHolder.manifest
        ApplicationContext ctx = grailsApplication.mainContext
        
        def l = attrs.remove('locale') ?: ''
        String locale = ''
        if (l instanceof Locale || l instanceof CharSequence) {
            locale = l.toString()
        } else if (log.warnEnabled) {
            log.warn "Unknown type ${l.class.name} for attribute 'locale'; use default locale."
        }
        locale = locale.replace('-', '_')
        if (log.debugEnabled) {
            log.debug "Retrieving i18n messages for locale ${locale}…"
        }

        String name = attrs.remove('name') ?: 'messages'
        String [] parts = locale.split('_')

        String src = null
        for (int i = parts.length; i >= 0 && !src; --i) {
            StringBuilder buf = new StringBuilder(name)
            for (int j = 0; j < i; j++) {
                buf << '_' << parts[j]
            }
            buf << '.js'
            String assetName = buf.toString()
            if (log.debugEnabled) {
                log.debug "Trying to find asset ${assetName}…"
            }

            if (manifest) {
    			String fileUri = manifest?.getProperty(assetName, assetName)
                Resource file = ctx.getResource("assets/${fileUri}")
				if (!file.exists()) {
					file = ctx.getResource("classpath:assets/${fileUri}")
				}
                if (file.exists()) {
                    src = assetName
                    break
                }
            } else {
                AssetFile file =
                    AssetHelper.fileForUri(assetName, 'application/javascript')
                if (file != null) {
                    src = assetName
                    break
                }
            }
        }
        if (log.debugEnabled) {
            if (src != null) {
                log.debug "Found asset '${src}'."
            } else {
                log.debug "Localized asset not found - using default asset '${name}.js'"
            }
        }
        if(src){
            out << asset.javascript(src: src ?: (name + '.js'))
        }
    }
}

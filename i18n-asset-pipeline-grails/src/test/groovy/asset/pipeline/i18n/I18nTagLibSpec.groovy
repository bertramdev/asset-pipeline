/*
 * I18nTagLibSpec.groovy
 *
 * Copyright (c) 2014-2015, Daniel Ellermann
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
import asset.pipeline.GenericAssetFile
import asset.pipeline.fs.AssetResolver
import asset.pipeline.grails.AssetProcessorService
import grails.test.mixin.TestFor
import spock.lang.Specification


@TestFor(I18nTagLib)
class I18nTagLibSpec extends Specification {

    //-- Fixture methods ------------------------

    // def setup() {
    //     tagLib.assetProcessorService = Mock(AssetProcessorService)
    //     tagLib.assetProcessorService.assetMapping >> 'assets'

    //     def assetsTagLib = mockTagLib(asset.pipeline.grails.AssetsTagLib)
    //     assetsTagLib.javascript = { attrs ->
    //         out << "<script src=\"${attrs.src}\"></script>".toString()
    //     }
    //     assetsTagLib.javascript.delegate = assetsTagLib
    // }


    // //-- Feature methods ------------------------

    // def 'The namespace is correct'() {
    //     'asset' == I18nTagLib.namespace
    // }

    // def 'No name renders default resource'() {
    //     expect:
    //     '<script src="messages.js"></script>' == applyTemplate('<asset:i18n/>')
    // }

    // def 'Given name renders stated resource'() {
    //     expect:
    //     '<script src="texts.js"></script>' == applyTemplate(
    //         '<asset:i18n name="texts"/>'
    //     )
    // }

    // def 'Available assets are searched for a locale'() {
    //     given: 'a mocked asset resolver'
    //     AssetResolver resolver = Mock()
    //     1 * resolver.getAsset('texts_de_DE_platt.js', 'application/javascript', null, null) >> null
    //     2 * resolver.getAsset('texts_de_DE.js', 'application/javascript', null, null) >> null
    //     3 * resolver.getAsset('texts_de.js', 'application/javascript', null, null) >> new GenericAssetFile()
    //     makeAssetResolvers resolver

    //     expect:
    //     '<script src="texts_de.js"></script>' == applyTemplate(
    //         '<asset:i18n name="texts" locale="de-DE-platt"/>'
    //     )
    //     '<script src="texts_de.js"></script>' == applyTemplate(
    //         '<asset:i18n name="texts" locale="de-DE"/>'
    //     )
    //     '<script src="texts_de.js"></script>' == applyTemplate(
    //         '<asset:i18n name="texts" locale="de"/>'
    //     )
    // }

    // def 'The most specific assets is used for a locale'() {
    //     given: 'a mocked asset resolver'
    //     AssetResolver resolver = Mock()
    //     1 * resolver.getAsset('texts_de_DE_platt.js', 'application/javascript', null, null) >> new GenericAssetFile()
    //     1 * resolver.getAsset('texts_de_DE.js', 'application/javascript', null, null) >> null
    //     2 * resolver.getAsset('texts_de.js', 'application/javascript', null, null) >> null
    //     makeAssetResolvers resolver

    //     expect:
    //     '<script src="texts_de_DE_platt.js"></script>' == applyTemplate(
    //         '<asset:i18n name="texts" locale="de-DE-platt"/>'
    //     )
    //     '<script src="texts.js"></script>' == applyTemplate(
    //         '<asset:i18n name="texts" locale="de-DE"/>'
    //     )
    //     '<script src="texts.js"></script>' == applyTemplate(
    //         '<asset:i18n name="texts" locale="de"/>'
    //     )
    // }

    // def 'A manifest is used to translate assets'() {
    //     given: 'a manifest'
    //     makeManifest()

    //     expect:
    //     '<script src="texts_de.js"></script>' == applyTemplate(
    //         '<asset:i18n name="texts" locale="de-DE-platt"/>'
    //     )
    //     '<script src="texts_de.js"></script>' == applyTemplate(
    //         '<asset:i18n name="texts" locale="de-DE"/>'
    //     )
    //     '<script src="texts_de.js"></script>' == applyTemplate(
    //         '<asset:i18n name="texts" locale="de"/>'
    //     )
    // }


    //-- Non-public methods ---------------------

    private void makeAssetResolvers(AssetResolver resolver) {
        AssetPipelineConfigHolder.resolvers = [resolver]
    }

    private void makeManifest() {
        def manifest = new Properties()
        manifest.setProperty(
            'texts_de_DE_platt.js',
            'texts_de_DE_platt-e361679d5548b0232a80d64ce203a450.js'
        )
        manifest.setProperty(
            'texts_de_DE.js',
            'texts_de_DE-f8b53bebc5a8489d2132b716eb0de458.js'
        )
        manifest.setProperty(
            'texts_de.js',
            'texts_de-63afa4054431cd255dd522314d42fe01.js'
        )
        manifest.setProperty(
            'texts.js',
            'texts-a81f00910a701c842ede4f497c191c80.js'
        )

        AssetPipelineConfigHolder.manifest = manifest
    }
}

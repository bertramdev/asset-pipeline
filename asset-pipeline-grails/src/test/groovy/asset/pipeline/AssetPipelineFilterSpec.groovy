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

package asset.pipeline

import spock.lang.Specification
import asset.pipeline.fs.FileSystemAssetResolver

// @TestMixin(GrailsUnitTestMixin)
class AssetPipelineFilterSpec extends Specification {
    AssetPipelineFilter filter
    Properties manifest

    void setup() {
        filter = new AssetPipelineFilter(applicationContext: [grailsApplication: grailsApplication])
        manifest = new Properties()
        grailsApplication.config.grails.assets.manifest = manifest
    }

    // void "current etag should be fileUri when manifest is missing"() {
    //     given:
    //     grailsApplication.config.grails.assets.manifest = null
    //     String fileUri = "favicon.ico"

    //     when:
    //     String etag = filter.getCurrentETag(fileUri)


    //     then:
    //     assert etag == fileUri
    // }


    // void "current etag should be fileUri when not in manifest"() {
    //     given:
    //     String fileUri = "favicon.ico"

    //     when:
    //     String etag = filter.getCurrentETag(fileUri)

    //     then:
    //     assert etag == fileUri
    // }

    // void "current etag should come from manifest when present"() {
    //     given:
    //     String fileUri = "favicon.ico"
    //     String digestUri = "favicon-9ef27019cc7a636e29ecc851528f716e.ico"
    //     manifest.setProperty(fileUri, digestUri)

    //     when:
    //     String etag = filter.getCurrentETag(fileUri)

    //     then:
    //     assert etag == digestUri
    // }
}

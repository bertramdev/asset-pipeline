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

package asset.pipeline.gradle

/**
 * Allows configuration of the Gradle plugin
 *
 * @author David Estes
 * @author Graeme Rocher
 */
class AssetPipelineExtension {
    boolean minifyJs = true
    boolean enableSourceMaps = true
    boolean minifyCss = true
    boolean enableDigests = true
    boolean skipNonDigests = false
    boolean enableGzip = true
    boolean packagePlugin=false
    boolean developmentRuntime=true
    boolean verbose = true
    Integer maxThreads=null
    String compileDir = 'build/assets'
    String assetsPath = 'src/assets'
	String jarTaskName
    Map minifyOptions
    Map configOptions

    List excludesGzip
    List excludes = []
    List includes = []
    List<String> resolvers = []

    void from(String resolverPath) {
        resolvers += resolverPath
    }
    
    Map toMap() {
        return [minifyJs: minifyJs, minifyCss: minifyCss, minifyOptions: minifyOptions, compileDir: compileDir, enableGzip: enableGzip, skipNonDigests: skipNonDigests, enableDigests: enableDigests, excludesGzip: excludesGzip, enableSourceMaps: enableSourceMaps, maxThreads: maxThreads]
    }
}

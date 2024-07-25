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

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * Allows configuration of the Gradle plugin
 *
 * @author David Estes
 * @author Graeme Rocher
 */
interface AssetPipelineExtension {
    @Input
    boolean getMinifyJs()
    void setMinifyJs(boolean value)
    @Input
    boolean getEnableSourceMaps()
    void setEnableSourceMaps(boolean value)
    @Input
    boolean getMinifyCss()
    void setMinifyCss(boolean value)
    @Input
    boolean getEnableDigests()
    void setEnableDigests(boolean value)
    @Input
    boolean getSkipNonDigests()
    void setSkipNonDigests(boolean value)
    @Input
    boolean getEnableGzip()
    void setEnableGzip(boolean value)
    @Input
    boolean getPackagePlugin()
    void setPackagePlugin(boolean value)
    @Input
    boolean getDevelopmentRuntime()
    void setDevelopmentRuntime(boolean value)
    @Input
    boolean getVerbose()
    void setVerbose(boolean value)
    @Input
    @Optional
    Integer getMaxThreads()
    void setMaxThreads(Integer value)
    @Input
    @Optional
    String getCompileDir()
    void setCompileDir(String value)
    @Input
    @Optional
    String getAssetsPath()
    void setAssetsPath(String value)
    @Input
    @Optional
	String getJarTaskName()
    void setJarTaskName(String value)
    @Input
    @Optional
    Map getMinifyOptions()
    void setMinifyOptions(Map value)
    @Input
    @Optional
    Map getConfigOptions()
    void setConfigOptions(Map value)

    @Input
    @Optional
    List getExcludesGzip()
    void setExcludesGzip(List value)
    @Input
    @Optional
    List getExcludes()
    void setExcludes(List value)
    @Input
    @Optional
    List getIncludes()
    void setIncludes(List value)
    @Input
    @Optional
    List<String> getResolvers()
    void setResolvers(List<String> value)

}

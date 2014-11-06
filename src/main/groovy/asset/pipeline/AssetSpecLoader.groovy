package asset.pipeline

import groovy.util.logging.Commons

/*
 * Copyright 2014 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Loads asset specification
 *
 * @author Graeme Rocher
 */
@Commons
class AssetSpecLoader {

    static final String FACTORIES_RESOURCE_LOCATION = "META-INF/asset-pipeline/asset.specs"

    private static List<Class<AssetFile>> specifications = null
    /**
     * Load the specifications and return a list of specification classes
     *
     * @param classLoader The classloader
     *
     * @return A list of specification classes
     */
    static List<Class<AssetFile>> loadSpecifications(ClassLoader classLoader = Thread.currentThread().contextClassLoader) {

        if(specifications == null) {
            def resources = classLoader.getResources(FACTORIES_RESOURCE_LOCATION)
            specifications = []

            resources.each { URL res ->
                def classNames = res.getText('UTF-8').split(System.getProperty('line.separator')).collect()  { String str -> str.trim() }

                for(className in classNames) {
                    try {
                        def cls = classLoader.loadClass(className)
                        if(AssetFile.isAssignableFrom(cls) ) {
                            if(!specifications.contains(cls))
                                specifications << (Class<AssetFile>) cls
                        }
                        else {
                            log.warn("Asset specification $className not regsistered because it does not implement the AssetFile interface")
                        }
                    } catch (Throwable e) {
                        log.error("Error loading asset specification $className: $e.message", e)
                    }
                }
            }
        }

        return specifications
    }
}

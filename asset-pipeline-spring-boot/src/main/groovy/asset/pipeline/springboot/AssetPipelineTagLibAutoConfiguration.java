/*
 * Copyright 2026 the original author or authors.
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
package asset.pipeline.springboot;

import asset.pipeline.grails.AssetMethodTagLib;
import asset.pipeline.grails.AssetProcessorService;
import asset.pipeline.grails.AssetsTagLib;

import grails.core.GrailsApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * The {@code <asset:...>} tag libraries, for a Spring Boot application rendering its views with GSP.
 *
 * <p>A Grails application finds them by scanning the artefacts of the plugin that carries them. An
 * application with no plugins has them as beans of its context instead, which GSP's tag library
 * lookup registers, and the tags are then usable in a page exactly as the ones GSP contributes
 * itself.
 *
 * <p>Both are declared because the first builds every URL by calling the second, which is what puts
 * {@code assetPath} in the default namespace - as a Grails application gets both, being artefacts of
 * the same plugin.
 */
@AutoConfiguration(afterName = { "asset.pipeline.grails.AssetPipelineAutoConfiguration", "grails.gsp.boot.GspAutoConfiguration" })
@ConditionalOnClass({ AssetsTagLib.class, GrailsApplication.class })
// The service the tag libraries read asset URLs from, and the lookup that registers a tag library
// with GSP - a tag library initializes itself against it, so it is what makes these beans useful.
@ConditionalOnBean(value = AssetProcessorService.class, type = "org.grails.taglib.TagLibraryLookup")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AssetPipelineTagLibAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AssetsTagLib assetsTagLib(AssetProcessorService assetProcessorService, GrailsApplication grailsApplication) {
        AssetsTagLib assetsTagLib = new AssetsTagLib();
        assetsTagLib.setAssetProcessorService(assetProcessorService);
        assetsTagLib.setGrailsApplication(grailsApplication);
        return assetsTagLib;
    }

    @Bean
    @ConditionalOnMissingBean
    public AssetMethodTagLib assetMethodTagLib(AssetProcessorService assetProcessorService) {
        AssetMethodTagLib assetMethodTagLib = new AssetMethodTagLib();
        assetMethodTagLib.setAssetProcessorService(assetProcessorService);
        return assetMethodTagLib;
    }

}

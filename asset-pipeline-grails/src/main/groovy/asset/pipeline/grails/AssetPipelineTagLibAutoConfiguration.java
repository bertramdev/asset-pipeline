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
package asset.pipeline.grails;

import java.util.Map;

import asset.pipeline.AssetPipelineConfigHolder;

import grails.core.GrailsApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Wires this plugin's tag libraries for an application that renders GSP views without being a Grails
 * application.
 *
 * <p>A Grails application finds them by scanning the artefacts of this plugin, and everything they
 * need is set up by the plugin descriptor as the application context is built. An application with
 * no plugins runs none of that: GSP is auto-configured for it instead, and the tag libraries have to
 * be beans of its context, which is what this contributes.
 *
 * <p>The condition is the tag library lookup GSP registers for a standalone application. A Grails
 * application has the plugin's own lookup instead, so nothing here applies to it and the beans the
 * plugin registers stay the only ones.
 *
 * <p>What this contributes is the tag libraries and the settings they read. What serves the assets
 * they point at - the resolvers, and the manifest that gives a built asset its digest name - comes
 * from {@code asset-pipeline-spring-boot}, which a standalone application adds alongside this. A
 * standalone application with this module and not that one gets tag libraries that render
 * development markup and non-digest urls, because nothing has told the pipeline where the assets
 * are.
 */
@AutoConfiguration(afterName = {
        "grails.gsp.boot.GspAutoConfiguration",
        "asset.pipeline.grails.AssetPipelineAutoConfiguration"
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBean(value = AssetProcessorService.class, type = "org.grails.web.pages.StandaloneTagLibraryLookup")
public class AssetPipelineTagLibAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AssetPipelineTagLibAutoConfiguration.class);

    /**
     * The tag library the pages use. What it takes is given to it here rather than found by name, as
     * a Grails application would: the service it asks for asset URLs, and the application it reads
     * its configuration from.
     */
    @Bean
    @ConditionalOnMissingBean
    public AssetsTagLib assetsTagLib(AssetProcessorService assetProcessorService, GrailsApplication grailsApplication) {
        AssetsTagLib assetsTagLib = new AssetsTagLib();
        assetsTagLib.setAssetProcessorService(assetProcessorService);
        assetsTagLib.setGrailsApplication(grailsApplication);
        return assetsTagLib;
    }

    /**
     * The second tag library this plugin carries, which puts {@code assetPath} in the default
     * namespace. The one above builds every URL by calling it, so both are needed - as a Grails
     * application gets both, being artefacts of the same plugin.
     */
    @Bean
    @ConditionalOnMissingBean
    public AssetMethodTagLib assetMethodTagLib(AssetProcessorService assetProcessorService) {
        AssetMethodTagLib assetMethodTagLib = new AssetMethodTagLib();
        assetMethodTagLib.setAssetProcessorService(assetProcessorService);
        return assetMethodTagLib;
    }

    /**
     * Hands the pipeline the {@code grails.assets} settings, which the plugin descriptor does for a
     * Grails application. Without this the settings an application writes are read by the tag
     * libraries but not by the pipeline itself, so a configured {@code mapping} is quietly ignored
     * while every other setting is honoured.
     */
    @Bean
    @ConditionalOnMissingBean(name = "assetPipelineConfiguration")
    public InitializingBean assetPipelineConfiguration(GrailsApplication grailsApplication) {
        return () -> {
            Map<String, Object> configured = grailsApplication.getConfig().getProperty("grails.assets", Map.class,
                    java.util.Collections.emptyMap());
            Map<String, Object> settings = flatten(configured);
            Map<String, Object> held = AssetPipelineConfigHolder.getConfig();
            if (held != null && !held.isEmpty() && !held.equals(settings)) {
                // The pipeline keeps its settings on a static, which outlives an application context:
                // a devtools reload builds a new context in the same JVM and finds the settings of
                // the one before it. The application being built now is the one that asked, so its
                // settings win - the alternative is a changed setting that is quietly ignored until
                // the JVM restarts.
                log.debug("Replacing the asset pipeline settings held from an earlier context: {} -> {}",
                        held, settings);
            }
            AssetPipelineConfigHolder.setConfig(settings);
        };
    }

    /**
     * Grails settings arrive as a navigable map, whose nested entries the pipeline reads by their
     * dotted names; anything else is already flat.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> flatten(Map<String, Object> configured) {
        if (configured instanceof org.grails.config.NavigableMap) {
            return ((org.grails.config.NavigableMap) configured).toFlatConfig();
        }
        return configured;
    }

}

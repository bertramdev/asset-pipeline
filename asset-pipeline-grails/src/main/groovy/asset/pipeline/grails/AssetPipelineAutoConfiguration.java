package asset.pipeline.grails;

import grails.config.Settings;
import grails.core.GrailsApplication;
import grails.util.Environment;
import grails.web.mapping.LinkGenerator;
import groovy.transform.CompileStatic;
import org.grails.plugins.web.mapping.UrlMappingsAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;


@CompileStatic
@AutoConfiguration(before = { UrlMappingsAutoConfiguration.class })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class AssetPipelineAutoConfiguration {
    @Value("${" + Settings.WEB_LINK_GENERATOR_USE_CACHE + ":#{null}}")
    private Boolean cacheUrls;

    @Value("${" + Settings.SERVER_URL + ":#{null}}")
    private String serverURL;

    @Bean
    LinkGenerator grailsLinkGenerator(AssetProcessorService assetProcessorService) {
        if (cacheUrls == null) {
            cacheUrls = !Environment.isDevelopmentMode() && !Environment.getCurrent().isReloadEnabled();
        }
        return cacheUrls ? new AssetSupportingCachingLinkGenerator(serverURL, assetProcessorService) : new AssetSupportingLinkGenerator(serverURL, assetProcessorService);
    }

    @Bean
    AssetProcessorService assetProcessorService() {
        return new AssetProcessorService();
    }
}

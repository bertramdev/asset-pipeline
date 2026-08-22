package asset.pipeline.springboot

import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.ClasspathAssetResolver
import asset.pipeline.fs.FileSystemAssetResolver
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

/**
 * The condition is here rather than only on the auto-configuration because this is what defines
 * the filter: an application that still names this package in its component scan registers this
 * class itself, and would otherwise get a filter it had switched off.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = AssetPipelineAutoConfiguration.ENABLED, matchIfMissing = true)
class AssetPipelineService {

	// The context itself, rather than the one the servlet context holds: it is the same context,
	// and it is there before a servlet container is.
	@Autowired
	ResourceLoader resourceLoader

	@Bean
	public FilterRegistrationBean assetPipelineFilterBean() {
		def manifestProps = new Properties()

		def manifestFile = resourceLoader.getResource("classpath:assets/manifest.properties")
		if(!manifestFile.exists()) {
			manifestFile = resourceLoader.getResource("assets/manifest.properties")
		}

		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		if(!manifestFile.exists()) {
			log.debug("Cant find manifest file!")
			def applicationResolver= new FileSystemAssetResolver('application','src/assets')
			AssetPipelineConfigHolder.registerResolver(applicationResolver)
			AssetPipelineConfigHolder.registerResolver(new ClasspathAssetResolver('classpath','META-INF/assets', "META-INF/assets.list"))
            AssetPipelineConfigHolder.registerResolver(new ClasspathAssetResolver('classpath','META-INF/static'))
            AssetPipelineConfigHolder.registerResolver(new ClasspathAssetResolver('classpath','META-INF/resources'))
			AssetPipelineDevFilter filter = new AssetPipelineDevFilter();
			registrationBean.setFilter(filter);
		}
		else {
			try {
				manifestProps.load(manifestFile.inputStream)
				AssetPipelineConfigHolder.manifest = manifestProps
				} catch(e) {
					log.warn "Failed to load Manifest",e
				}
				AssetPipelineFilter filter = new AssetPipelineFilter();
				registrationBean.setFilter(filter);
		}
		registrationBean.urlPatterns = ["/assets/*".toString()]
		registrationBean.setOrder(0);
		return registrationBean;
	}
}

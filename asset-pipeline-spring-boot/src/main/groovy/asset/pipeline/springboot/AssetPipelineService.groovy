package asset.pipeline.springboot
import org.springframework.boot.context.embedded.*
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.FileSystemAssetResolver
import groovy.util.logging.Log4j
import javax.servlet.ServletContext
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.context.WebApplicationContext
import org.springframework.core.io.ResourceLoader

@Log4j
@Configuration
class AssetPipelineService {

	@Autowired
	ServletContext servletContext;

	@Bean
	public FilterRegistrationBean assetPipelineFilterBean() {
		def manifestProps = new Properties()

		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext)
		def manifestFile = applicationContext.getResource("classpath:assets/manifest.properties")

		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		if(!manifestFile.exists()) {
			def applicationResolver= new FileSystemAssetResolver('application','src/assets')
			AssetPipelineConfigHolder.registerResolver(applicationResolver)
			AssetPipelineConfigHolder.registerResolver(new SpringResourceAssetResolver('classpath',applicationContext, 'META-INF/assets'))
            AssetPipelineConfigHolder.registerResolver(new SpringResourceAssetResolver('classpath',applicationContext, 'META-INF/static'))
            AssetPipelineConfigHolder.registerResolver(new SpringResourceAssetResolver('classpath',applicationContext, 'META-INF/resources'))
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

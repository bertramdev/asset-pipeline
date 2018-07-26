package asset.pipeline.springboot
import javax.servlet.ServletContext

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils

import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.ClasspathAssetResolver
import asset.pipeline.fs.FileSystemAssetResolver
import groovy.util.logging.Log4j

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
		if(!manifestFile.exists()) {
			manifestFile = applicationContext.getResource("assets/manifest.properties")
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

package asset.pipeline.springboot

import asset.pipeline.*;
import java.util.*;
import javax.servlet.http.*;
import org.springframework.context.annotation.*;
import org.springframework.core.io.*;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.*;

/**
* Auto configuration class for asset-pipeline and Thymeleaf for auto resolving digest 
* named assets for asset-pipeline
* @author Oleg Oshmyan (astiob)
*/
@Configuration
public class WebMvcConfigurer extends WebMvcConfigurerAdapter {
	private static class AssetResolver extends AbstractResourceResolver {
		@Override
		protected Resource
		resolveResourceInternal(HttpServletRequest request,
		                        String requestPath,
		                        List<? extends Resource> locations,
		                        ResourceResolverChain chain) {
			return chain.resolveResource(request, requestPath, locations);
		}

		@Override
		protected String
		resolveUrlPathInternal(String resourceUrlPath,
		                       List<? extends Resource> locations,
		                       ResourceResolverChain chain) {
			String path = resolveAssetPath(resourceUrlPath);
			if (path != null)
				return path;
			return chain.resolveUrlPath(resourceUrlPath, locations);
		}

		private static String resolveAssetPath(String path) {
			Properties manifest = AssetPipelineConfigHolder.manifest;
			if (manifest == null)
				return null;

			if (path.startsWith("/"))
				path = path.substring(1);
			return manifest.getProperty(path);
		}
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/assets/*")
		        .resourceChain(true)
		        .addResolver(new AssetResolver());
	}
}
package asset.pipeline.springboot


import asset.pipeline.servlet.AssetPipelineFilterCore
import asset.pipeline.servlet.AssetPipelineServletResource
import asset.pipeline.servlet.AssetPipelineServletResourceRepository
import groovy.util.logging.Log4j
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import org.springframework.core.io.Resource
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils


@Log4j
class AssetPipelineFilter implements Filter {
	AssetPipelineFilterCore assetPipelineFilterCore = new AssetPipelineFilterCore()

	@Override
	void init(FilterConfig config) throws ServletException {
		assetPipelineFilterCore.servletContext = config.servletContext
		assetPipelineFilterCore.mapping = "assets"

		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.servletContext)
		assetPipelineFilterCore.assetPipelineServletResourceRepository = new SpringServletResourceRepository(applicationContext)
	}

	@Override
	void destroy() {
	}

	@Override
	void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		assetPipelineFilterCore.doFilter(request, response, chain)
	}

	private static final class SpringServletResourceRepository implements AssetPipelineServletResourceRepository {
		private final WebApplicationContext applicationContext
		SpringServletResourceRepository(WebApplicationContext applicationContext) {
			this.applicationContext = applicationContext
		}

		@Override
		AssetPipelineServletResource getResource(String path) {
			return SpringServletResource.create(applicationContext.getResource("classpath:assets${path}"))
		}

		@Override
		AssetPipelineServletResource getGzippedResource(String path) {
			return SpringServletResource.create(applicationContext.getResource("assets${path}.gz"))
		}
	}

	private static final class SpringServletResource implements AssetPipelineServletResource {
		private final Resource resource

		private SpringServletResource(Resource resource) {
			this.resource = resource
		}

		static SpringServletResource create(Resource resource) {
			if (!resource.exists()) {
				return null
			}

			new SpringServletResource(resource)
		}

		@Override
		Long getLastModified() {
			return resource.lastModified()
		}

		@Override
		InputStream getInputStream() {
			return resource.getInputStream()
		}
	}
}

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
	void init(final FilterConfig config) throws ServletException {
		assetPipelineFilterCore.servletContext = config.servletContext
		assetPipelineFilterCore.mapping = "assets"

		final WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.servletContext)
		assetPipelineFilterCore.assetPipelineServletResourceRepository = new SpringServletResourceRepository(applicationContext)
	}

	@Override
	void destroy() {
	}

	@Override
	void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		assetPipelineFilterCore.doFilter(request, response, chain)
	}

	private static final class SpringServletResourceRepository implements AssetPipelineServletResourceRepository {

		private final WebApplicationContext applicationContext


		SpringServletResourceRepository(final WebApplicationContext applicationContext) {
			this.applicationContext = applicationContext
		}


		@Override
		AssetPipelineServletResource getResource(final String path) {
			def resource = applicationContext.getResource("classpath:assets/${path}")
			if(!resource.exists()) {
				resource = applicationContext.getResource("assets/${path}")
			}
			return SpringServletResource.create(resource)
		}

		@Override
		AssetPipelineServletResource getGzippedResource(final String path) {
			def resource = applicationContext.getResource("classpath:assets/${path}.gz")
			if(!resource.exists()) {
				resource = applicationContext.getResource("assets/${path}.gz")
			}
			return SpringServletResource.create(resource)
		}
	}

	private static final class SpringServletResource implements AssetPipelineServletResource {

		private final Resource resource


		private SpringServletResource(final Resource resource) {
			this.resource = resource
		}


		static SpringServletResource create(final Resource resource) {
			if(!resource.exists()) {
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

package asset.pipeline.springboot

import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.AssetPipelineResponseBuilder
import asset.pipeline.servlet.AssetPipelineFilterCore
import asset.pipeline.servlet.AssetPipelineServletResource
import asset.pipeline.servlet.AssetPipelineServletResourceRepository
import groovy.util.logging.Log4j
import org.springframework.core.io.Resource
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils

import javax.servlet.*
import java.text.SimpleDateFormat

@Log4j
class AssetPipelineFilter implements Filter {
    AssetPipelineFilterCore assetPipelineFilterCore = new AssetPipelineFilterCore()

    void init(FilterConfig config) throws ServletException {
        assetPipelineFilterCore.servletContext = config.servletContext
        assetPipelineFilterCore.mapping = "assets"

        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.servletContext)
        assetPipelineFilterCore.assetPipelineServletResourceRepository = new SpringServletResourceRepository(applicationContext)
    }

    void destroy() {
    }

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        assetPipelineFilterCore.doFilter(request, response, chain)
    }

    private static final class SpringServletResourceRepository implements AssetPipelineServletResourceRepository {
        private final WebApplicationContext applicationContext
        public SpringServletResourceRepository(WebApplicationContext applicationContext) {
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

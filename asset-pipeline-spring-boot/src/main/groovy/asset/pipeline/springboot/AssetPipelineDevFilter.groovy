package asset.pipeline.springboot

import asset.pipeline.servlet.AssetPipelineDevFilterCore
import groovy.util.logging.Log4j

import javax.servlet.*

@Log4j
class AssetPipelineDevFilter implements Filter {
    AssetPipelineDevFilterCore assetPipelineDevFilterCoreStandalone = new AssetPipelineDevFilterCore()

    void init(FilterConfig config) throws ServletException {
        assetPipelineDevFilterCoreStandalone.servletContext = config.servletContext
        assetPipelineDevFilterCoreStandalone.mapping = "assets"
    }

    void destroy() {
    }

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        assetPipelineDevFilterCoreStandalone.doFilter(request, response, chain)
    }

}

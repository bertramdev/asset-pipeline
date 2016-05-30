package asset.pipeline.springboot


import asset.pipeline.servlet.AssetPipelineDevFilterCore
import groovy.util.logging.Log4j
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse


@Log4j
class AssetPipelineDevFilter implements Filter {

	AssetPipelineDevFilterCore assetPipelineDevFilterCoreStandalone = new AssetPipelineDevFilterCore()


	@Override
	void init(final FilterConfig config) throws ServletException {
		assetPipelineDevFilterCoreStandalone.servletContext = config.servletContext
		assetPipelineDevFilterCoreStandalone.mapping = "assets"
	}

	@Override
	void destroy() {
	}

	@Override
	void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		assetPipelineDevFilterCoreStandalone.doFilter(request, response, chain)
	}
}

package asset.pipeline.servlet


import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse


class AssetPipelineFilter implements Filter {

	AssetPipelineFilterCore assetPipelineFilterCore = new AssetPipelineFilterCore()


	void setAssetPipelineServletResourceRepository(final AssetPipelineServletResourceRepository assetPipelineServletResourceRepository) {
		assetPipelineFilterCore.assetPipelineServletResourceRepository = assetPipelineServletResourceRepository
	}

	void setMapping(final String mapping) {
		assetPipelineFilterCore.mapping = mapping
	}

	@Override
	void init(final FilterConfig filterConfig) throws ServletException {
		assetPipelineFilterCore.servletContext = filterConfig.getServletContext()
	}

	@Override
	void destroy() {
	}

	@Override
	void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		assetPipelineFilterCore.doFilter(request, response, chain)
	}
}

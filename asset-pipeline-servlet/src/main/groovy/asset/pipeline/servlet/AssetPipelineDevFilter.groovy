package asset.pipeline.servlet


import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse


class AssetPipelineDevFilter implements Filter {
	AssetPipelineDevFilterCore assetPipelineDevFilterCore = new AssetPipelineDevFilterCore()

	void setMapping(String mapping) {
		assetPipelineDevFilterCore.mapping = mapping
	}

	@Override
	void init(FilterConfig filterConfig) throws ServletException {
		assetPipelineDevFilterCore.servletContext = filterConfig.getServletContext()
	}

	@Override
	void destroy() {

	}

	@Override
	void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		assetPipelineDevFilterCore.doFilter(request, response, chain)
	}
}

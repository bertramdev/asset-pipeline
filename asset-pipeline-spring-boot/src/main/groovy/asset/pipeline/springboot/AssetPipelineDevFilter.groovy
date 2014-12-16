package asset.pipeline.springboot
import asset.pipeline.*

import javax.servlet.*
import org.springframework.web.context.support.WebApplicationContextUtils
import groovy.util.logging.Log4j

@Log4j
class AssetPipelineDevFilter implements Filter {
	def applicationContext
	def servletContext
	void init(FilterConfig config) throws ServletException {
		applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.servletContext)
		servletContext = config.servletContext
		// permalinkService = applicationContext['spudPermalinkService']
	}

	void destroy() {
	}

	void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		def mapping = 'assets'

		def fileUri = request.requestURI
		def baseAssetUrl = request.contextPath == "/" ? "/$mapping/" : "${request.contextPath}/${mapping}/"
		if(fileUri.startsWith(baseAssetUrl)) {
			fileUri = fileUri.substring(baseAssetUrl.length())
		}
		def format = servletContext.getMimeType(request.requestURI)

		def fileContents = AssetPipeline.serveAsset(fileUri, format, null, request.characterEncoding)

		if (fileContents) {

			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
			response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			response.setDateHeader("Expires", 0); // Proxies.
			response.setContentType(format)
			try {
				response.outputStream << fileContents
				response.flushBuffer()
				} catch(e) {
					log.debug("File Transfer Aborted (Probably by the user)",e)
				}


		}

			if (!response.committed) {
				chain.doFilter(request, response)
			}
	}

}

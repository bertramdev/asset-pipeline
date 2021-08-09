package asset.pipeline.servlet


import asset.pipeline.AssetPipeline
import java.util.logging.Logger
import javax.servlet.FilterChain
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class AssetPipelineDevFilterCore {

	private final static Logger log = Logger.getLogger(getClass().getName())


	String mapping = "mapping"
	ServletContext servletContext


	void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if(request instanceof HttpServletRequest) {
			doFilterHttp(request, response, chain)
		} else {
			chain.doFilter(request, response)
		}
	}

	private void doFilterHttp(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) {
		String fileUri = request.requestURI
		final String baseAssetUrl = request.contextPath == "/" ? "/$mapping/" : "${request.contextPath}/${mapping}/"
		if(fileUri.startsWith(baseAssetUrl)) {
			fileUri = fileUri.substring(baseAssetUrl.length())
		}
		final String format = servletContext.getMimeType(request.requestURI)

		final byte[] fileContents
		if(request.getParameter('compile') == 'false') {
			fileContents = AssetPipeline.serveUncompiledAsset(fileUri, format, null, request.characterEncoding)
		} else {
			fileContents = AssetPipeline.serveAsset(fileUri, format, null, request.characterEncoding)
		}

		if(fileContents) {
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
			response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			response.setDateHeader("Expires", 0); // Proxies.
			response.setContentType(format)
			try {
				response.outputStream << fileContents
				response.flushBuffer()
			} catch(final e) {
				log.fine("File Transfer Aborted (Probably by the user): ${e.getMessage()}")
			}
		}

		if(!response.committed) {
			filterChain.doFilter(request, response)
		}
	}
}

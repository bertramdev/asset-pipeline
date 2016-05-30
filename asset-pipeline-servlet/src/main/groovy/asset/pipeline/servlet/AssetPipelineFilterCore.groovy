package asset.pipeline.servlet


import asset.pipeline.AssetPipelineResponseBuilder
import java.util.logging.Logger
import javax.servlet.FilterChain
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class AssetPipelineFilterCore {

	private static final Logger log = Logger.getLogger(getClass().getName())
	static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz"


	String mapping = "mapping"
	AssetPipelineServletResourceRepository assetPipelineServletResourceRepository
	ServletContext servletContext


	void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		if(request instanceof HttpServletRequest) {
			doFilterHttp(request, response, chain)
		} else {
			chain.doFilter(request, response)
		}
	}

	private void doFilterHttp(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) {
		if(assetPipelineServletResourceRepository == null) {
			throw new IllegalStateException("Property 'assetPipelineServletResourceRepository' is null")
		}

		String fileUri = request.requestURI
		final String baseAssetUrl = request.contextPath == "/" ? "/$mapping" : "${request.contextPath}/${mapping}"
		if(fileUri.startsWith(baseAssetUrl)) {
			fileUri = fileUri.substring(baseAssetUrl.length())
		}

		AssetPipelineServletResource resource = assetPipelineServletResourceRepository.getResource(fileUri)
		if(resource) {
			final Date lastModifiedDate = resource.getLastModified() ? new Date(resource.getLastModified()) : null
			final def responseBuilder = new AssetPipelineResponseBuilder(fileUri, request.getHeader('If-None-Match'), request.getHeader('If-Modified-Since'), lastModifiedDate)
			responseBuilder.headers.each { final header ->
				response.setHeader(header.key, header.value)
			}
			if(responseBuilder.statusCode) {
				response.status = responseBuilder.statusCode
			}

			if(response.status != 304) {
				// Check for GZip
				final def acceptsEncoding = request.getHeader("Accept-Encoding")
				if(acceptsEncoding?.split(",")?.contains("gzip")) {
					final AssetPipelineServletResource gzipResource = assetPipelineServletResourceRepository.getGzippedResource(fileUri)
					if(gzipResource) {
						resource = gzipResource
						response.setHeader('Content-Encoding', 'gzip')
					}
				}
				final def format = servletContext.getMimeType(request.requestURI)
				final def encoding = request.getCharacterEncoding()
				if(encoding) {
					response.setCharacterEncoding(encoding)
				}
				response.setContentType(format)
				final def inputStream
				try {
					final byte[] buffer = new byte[102400]
					int len
					inputStream = resource.inputStream
					final def out = response.outputStream
					while((len = inputStream.read(buffer)) != -1) {
						out.write(buffer, 0, len)
					}
					response.flushBuffer()
				} catch(final e) {
					log.fine("File Transfer Aborted (Probably by the user): ${e.getMessage()}")
				} finally {
					try { inputStream?.close() } catch(final ie) { /* silent fail*/}
				}
			} else {
				response.flushBuffer()
			}
		}

		if(!response.committed) {
			filterChain.doFilter(request, response)
		}
	}
}

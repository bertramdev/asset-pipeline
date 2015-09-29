package asset.pipeline.springboot

import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.AssetPipelineResponseBuilder
import groovy.util.logging.Log4j
import org.springframework.web.context.support.WebApplicationContextUtils

import javax.servlet.*
import java.text.SimpleDateFormat

@Log4j
class AssetPipelineFilter implements Filter {
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz"
    private final SimpleDateFormat sdf = new SimpleDateFormat(HTTP_DATE_FORMAT);

    def applicationContext
    def servletContext

    void init(FilterConfig config) throws ServletException {
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.servletContext)
        servletContext = config.servletContext
    }

    void destroy() {
    }

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        def mapping = 'assets'

        def fileUri = request.requestURI
        def baseAssetUrl = request.contextPath == "/" ? "/$mapping" : "${request.contextPath}/${mapping}"
        if (fileUri.startsWith(baseAssetUrl)) {
            fileUri = fileUri.substring(baseAssetUrl.length())
        }
        def file = applicationContext.getResource("classpath:assets${fileUri}")
        if (file.exists()) {
            //Do this early so a 304 will still contain 'Last-Modified' in the case that there is a CDN in between client and server
            response.setHeader('Last-Modified', getLastModifiedDate(file))
            if (checkETag(request, response, fileUri) && checkIfModifiedSince(request, file)) {
                // Check for GZip
                def acceptsEncoding = request.getHeader("Accept-Encoding")
                if (acceptsEncoding?.split(",")?.contains("gzip")) {
                    def gzipFile = applicationContext.getResource("assets${fileUri}.gz")
                    if (gzipFile.exists()) {
                        file = gzipFile
                        response.setHeader('Content-Encoding', 'gzip')
                    }
                }
                def format = servletContext.getMimeType(request.requestURI)
                def encoding = request.getCharacterEncoding()
                if (encoding) {
                    response.setCharacterEncoding(encoding)
                }
                response.setContentType(format)
                response.setHeader('Vary', 'Accept-Encoding')
                response.setHeader('Cache-Control', 'public, max-age=31536000')

                try {
                    response.outputStream << file.inputStream.getBytes()
                    response.flushBuffer()
                } catch (e) {
                    log.debug("File Transfer Aborted (Probably by the user)", e)
                }
            }

        }

        if (!response.committed) {
            chain.doFilter(request, response)
        }
    }

    /**
     * Here we check if the request is contingent upon an ETag and if not, we append the ETag to the header key.
     * This ETag is essentially the digested file name as it is unique unless the file changes.
     * @return Whether processing should continue or not
     */
    Boolean checkETag(ServletRequest request, ServletResponse response, fileUri) {
        String etagName = getCurrentETag(fileUri)

        def ifNoneMatchHeader = request.getHeader('If-None-Match')
        if (ifNoneMatchHeader && ifNoneMatchHeader == etagName) {
            response.status = 304
            response.flushBuffer()
            return false
        }
        response.setHeader('ETag', "$etagName")
        return true
    }

    Boolean checkIfModifiedSince(ServletRequest request, file) {
        String ifNoneMatchHeader = request.getHeader('If-Modified-Since')
        if (ifNoneMatchHeader && hasNotChanged(ifNoneMatchHeader, file)) {
            response.status = 304
            response.flushBuffer()
            return false
        }
        return true
    }

    boolean hasNotChanged(String ifModifiedSince, file) {
        boolean hasNotChanged = false
        if (ifModifiedSince) {
            try {
                hasNotChanged = new Date(file?.lastModified()) <= sdf.parse(ifModifiedSince)
            } catch (Exception e) {
                log.debug("Could not parse date time or file modified date", e)
            }
        }
        return hasNotChanged
    }

    String getCurrentETag(String fileUri) {
        def manifestPath = fileUri
        if (fileUri.startsWith('/')) {
            manifestPath = fileUri.substring(1) //Omit forward slash
        }

        def manifest = AssetPipelineConfigHolder.manifest

        return manifest?.getProperty(manifestPath) ?: manifestPath
    }

    private String getLastModifiedDate(file) {
        String lastModifiedDateTimeString = sdf.format(new Date())
        try {
            lastModifiedDateTimeString = sdf.format(new Date(file?.lastModified()))
        } catch (Exception e) {
            log.debug("Could not get last modified date time for file", e)
        }

        return lastModifiedDateTimeString
    }
}

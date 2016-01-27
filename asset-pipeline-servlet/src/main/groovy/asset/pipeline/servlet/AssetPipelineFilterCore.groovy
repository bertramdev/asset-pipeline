package asset.pipeline.servlet

import asset.pipeline.AssetPipelineConfigHolder

import javax.servlet.FilterChain
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat
import java.util.logging.Logger

class AssetPipelineFilterCore {
    private static final Logger log = Logger.getLogger(getClass().getName())
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz"

    String mapping = "mapping"
    AssetPipelineServletResourceRepository assetPipelineServletResourceRepository
    ServletContext servletContext

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            doFilterHttp(request, response, chain)
        } else {
            chain.doFilter(request, response)
        }
    }

    private void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        if (assetPipelineServletResourceRepository == null) {
            throw new IllegalStateException("Property 'assetPipelineServletResourceRepository' is null")
        }

        String fileUri = request.requestURI
        String baseAssetUrl = request.contextPath == "/" ? "/$mapping" : "${request.contextPath}/${mapping}"
        if (fileUri.startsWith(baseAssetUrl)) {
            fileUri = fileUri.substring(baseAssetUrl.length())
        }

        AssetPipelineServletResource resource = assetPipelineServletResourceRepository.getResource(fileUri)
        if (resource) {
            //Do this early so a 304 will still contain 'Last-Modified' in the case that there is a CDN in between client and server
            response.setHeader('Last-Modified', getLastModifiedDate(resource.getLastModified()))
            if (checkETag(request, response, fileUri) && checkIfModifiedSince(request, response, resource.getLastModified())) {
                // Check for GZip
                def acceptsEncoding = request.getHeader("Accept-Encoding")
                if (acceptsEncoding?.split(",")?.contains("gzip")) {
                    AssetPipelineServletResource gzipResource = assetPipelineServletResourceRepository.getGzippedResource(fileUri)
                    if (gzipResource) {
                        resource = gzipResource
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
                    response.outputStream << resource.inputStream.getBytes()
                    response.flushBuffer()
                } catch (e) {
                    log.fine("File Transfer Aborted (Probably by the user): ${e.getMessage()}")
                }
            }
        }

        if (!response.committed) {
            filterChain.doFilter(request, response)
        }
    }

    /**
     * Here we check if the request is contingent upon an ETag and if not, we append the ETag to the header key.
     * This ETag is essentially the digested file name as it is unique unless the file changes.
     * @return Whether processing should continue or not
     */
    private static Boolean checkETag(HttpServletRequest request, HttpServletResponse response, String fileUri) {
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

    private static Boolean checkIfModifiedSince(HttpServletRequest request, HttpServletResponse response, Long lastModified) {
        String ifNoneMatchHeader = request.getHeader('If-Modified-Since')
        if (ifNoneMatchHeader && hasNotChanged(ifNoneMatchHeader, lastModified)) {
            response.status = 304
            response.flushBuffer()
            return false
        }
        return true
    }

    private static boolean hasNotChanged(String ifModifiedSince, Long lastModified) {
        SimpleDateFormat sdf = new SimpleDateFormat(HTTP_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        boolean hasNotChanged = false
        if (ifModifiedSince) {
            try {
                hasNotChanged = new Date(lastModified) <= sdf.parse(ifModifiedSince)
            } catch (Exception e) {
                log.fine("Could not parse date time or file modified date: ${e.getMessage()}")
            }
        }
        return hasNotChanged
    }

    private static String getCurrentETag(String fileUri) {
        def manifestPath = fileUri
        if (fileUri.startsWith('/')) {
            manifestPath = fileUri.substring(1) //Omit forward slash
        }

        def manifest = AssetPipelineConfigHolder.manifest

        return manifest?.getProperty(manifestPath) ?: manifestPath
    }

    private static String getLastModifiedDate(Long lastModified) {
        SimpleDateFormat sdf = new SimpleDateFormat(HTTP_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String lastModifiedDateTimeString = sdf.format(new Date())
        try {
            lastModifiedDateTimeString = sdf.format(new Date(lastModified))
        } catch (Exception e) {
            log.fine("Could not get last modified date time for file: ${e.getMessage()}")
        }

        return lastModifiedDateTimeString
    }
}

package asset.pipeline

public class AssetPipelineResponseBuilder {
    public String uri
    public String ifNoneMatchHeader
    public String ifModifiedSinceHeader
    public Integer statusCode = 200

    public Map<String, String> headers = [:]

    AssetPipelineResponseBuilder(String uri, String ifNoneMatchHeader = null, String ifModifiedSinceHeader = null) {
        this.uri = uri
        this.ifNoneMatchHeader = ifNoneMatchHeader
        this.ifModifiedSinceHeader = ifModifiedSinceHeader

        if (checkETag()) {
            headers['Vary'] = 'Accept-Encoding'
            headers['Cache-Control'] = 'public, max-age=31536000'
        }
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getCurrentETag() {
        def manifestPath = uri
        if (uri.startsWith('/')) {
            manifestPath = uri.substring(1) //Omit forward slash
        }

        def manifest = AssetPipelineConfigHolder.manifest

        return manifest?.getProperty(manifestPath) ?: manifestPath
    }

    public Boolean checkETag() {
        String etagName = getCurrentETag()
        if (ifNoneMatchHeader && ifNoneMatchHeader == etagName) {
            statusCode = 304
            return false
        }
        headers["ETag"] = "$etagName"
        return true
    }
}

package asset.pipeline;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

public class AssetPipelineResponseBuilder {
	public String uri;
	public String ifNoneMatchHeader;
	public Integer statusCode = 200;
	public HashMap<String,String> headers ;

	public AssetPipelineResponseBuilder(String uri, String ifNoneMatchHeader) {
		this.uri = uri;
		this.ifNoneMatchHeader = ifNoneMatchHeader;
		this.headers = new HashMap<String,String>();
		if(checkETag()) {
			headers.put("Vary", "Accept-Encoding");
			headers.put("Cache-Control", "public, max-age=31536000");
		}
	}

	public AssetPipelineResponseBuilder(String uri) {
		this.uri = uri;
		this.headers = new HashMap<String,String>();
		if(checkETag()) {
			headers.put("Vary", "Accept-Encoding");
			headers.put("Cache-Control", "public, max-age=31536000");
		}
	}

	public Map<String,String> getHeaders() {
		return this.headers;
	}

	public Integer getStatusCode() {
		return this.statusCode;
	}

	public String getCurrentETag() {
		String manifestPath = uri;
		if(uri.startsWith("/")) {
			manifestPath = uri.substring(1); //Omit forward slash
		}

		Properties manifest = AssetPipelineConfigHolder.getManifest();
		if(manifest != null) {
			return manifest.getProperty(manifestPath, manifestPath);
		}
		return manifestPath;
	}

	public Boolean checkETag() {
		String etagName = getCurrentETag();
		if(ifNoneMatchHeader != null && ifNoneMatchHeader == etagName) {
			statusCode = 304;
			return false;
		}
		headers.put("ETag", etagName);
		return true;
	}
}

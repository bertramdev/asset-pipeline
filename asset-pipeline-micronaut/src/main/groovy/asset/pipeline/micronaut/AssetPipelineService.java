package asset.pipeline.micronaut;

import asset.pipeline.AssetPipeline;
import asset.pipeline.AssetPipelineConfigHolder;
import asset.pipeline.fs.ClasspathAssetResolver;
import asset.pipeline.fs.FileSystemAssetResolver;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.env.Environment;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.server.types.files.StreamedFile;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Singleton
public class AssetPipelineService {
	private static final Logger LOG = LoggerFactory.getLogger(AssetPipelineService.class);
	private static final String COMPILE_PARAM = "compile";
	static final ProductionAssetCache fileCache = new ProductionAssetCache();

	public Environment environment;

	// @Inject
	public AssetPipelineService() {
		// this.environment = environment;
		// AssetPipelineConfigHolder.setConfig(environment.getProperty("assets",Map.class).orElse(AssetPipelineConfigHolder.getConfig()));
		if(AssetPipelineConfigHolder.config.get("mapping") == null) {
			AssetPipelineConfigHolder.config.put("mapping",""); //root mapping by default
		}
		Properties manifestProps = new Properties();
		Enumeration<URL> manifestFiles = null;
		try {
			manifestFiles = this.getClass().getClassLoader().getResources("assets/manifest.properties");
		} catch(IOException exi) {
			LOG.warn("Error Scanning Classpath for Asset-Pipeline Manifest File: {}",exi.getMessage(),exi);
		}

		if(manifestFiles != null && manifestFiles.hasMoreElements()) {
			while(manifestFiles.hasMoreElements()) {
				try {
					InputStream currentManifestFile = manifestFiles.nextElement().openStream();
					manifestProps.load(currentManifestFile);
				} catch(IOException exi2) {
					LOG.warn("Error Loading Asset-Pipeline Manifest File: {}",exi2.getMessage(),exi2);
				}
			}
			AssetPipelineConfigHolder.manifest = manifestProps;
		} else {
			AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver("application", "src/assets"));
			AssetPipelineConfigHolder.registerResolver(new ClasspathAssetResolver("classpath", "META-INF/assets", "META-INF/assets.list"));
			AssetPipelineConfigHolder.registerResolver(new ClasspathAssetResolver("classpath", "META-INF/static"));
			AssetPipelineConfigHolder.registerResolver(new ClasspathAssetResolver("classpath", "META-INF/resources"));
		}
	}


	public boolean isDevMode() {
		if(AssetPipelineConfigHolder.manifest != null) {
			return false;
		}
		return true;
	}


	public Flowable<Optional<byte[]>> handleAssetDevMode(String filename, String contentType, String encoding, HttpRequest<?> request) {

		return Flowable.fromCallable(() -> {
			byte[] fileContents;

			if(shouldCompile(request)) {
				fileContents = AssetPipeline.serveAsset(filename,contentType,null, encoding);
			} else {
				fileContents = AssetPipeline.serveUncompiledAsset(filename,contentType,null, encoding);
			}

			if(fileContents == null && (filename.endsWith("/") || "".equals(filename))) {
				String indexFile =  "/".equals(filename) ? "/index.html" : String.format("%s/index.html", filename);

				if(shouldCompile(request)) {
					fileContents = AssetPipeline.serveAsset(indexFile,contentType,null, encoding);
				} else {
					fileContents = AssetPipeline.serveUncompiledAsset(indexFile,contentType,null, encoding);
				}

			}
			return Optional.ofNullable(fileContents);
		}).subscribeOn(Schedulers.io());
	}


	public Flowable<MutableHttpResponse<?>> handleAsset(String filename, MediaType contentType, String encoding, HttpRequest<?> request, ServerFilterChain chain) {

		if("".equals(filename) || filename.endsWith("/")) {
			filename += "index.html";
		}

		if(filename.startsWith("/")) {
			filename = filename.substring(1);
		}
		final Boolean isDigestVersion = isDigestVersion(filename);
		final String etagHeader = getCurrentETag(filename);
		final String acceptEncoding = request.getHeaders().get("Accept-Encoding");
		filename = AssetPipelineConfigHolder.manifest.getProperty(filename,filename);
		final String fileUri = filename ;
		final AssetAttributes attributeCache = fileCache.get(filename);
		Flowable<AssetAttributes> attributeFlowable;
		if(attributeCache != null) {
			attributeFlowable = Flowable.fromCallable(() -> attributeCache);
		} else {
			attributeFlowable = Flowable.fromCallable(() -> resolveAssetAttribute(fileUri));
		}



		return attributeFlowable.switchMap( assetAttribute -> {
			if(assetAttribute.exists()) {
				final Boolean gzipStream = acceptEncoding != null && acceptEncoding.contains("gzip") && assetAttribute.gzipExists();

				String ifNoneMatch = request.getHeaders().get("If-None-Match");
				if(ifNoneMatch != null && ifNoneMatch.equals(etagHeader)) {
					LOG.debug("NOT MODIFIED!");
					return Flowable.fromCallable(() -> HttpResponse.notModified());
				} else {
					LOG.debug("Generating Response");
					return Flowable.fromCallable(() -> {
						URLConnection urlCon = gzipStream ? assetAttribute.getGzipResource().openConnection() : assetAttribute.getResource().openConnection();
						StreamedFile streamedFile = new StreamedFile(urlCon.getInputStream(), contentType, urlCon.getLastModified(), urlCon.getContentLength() );
						MutableHttpResponse<StreamedFile> response = HttpResponse.ok(streamedFile);
						if(gzipStream) {
							response.header("Content-Encoding","gzip");
						}
						if(encoding != null) {
							response.characterEncoding(encoding);
						}
						response.contentType(contentType);
						response.header("ETag",etagHeader);
						response.header("Vary","Accept-Encoding");
						if(isDigestVersion && !fileUri.endsWith(".html")) {
							response.header("Cache-Control","public, max-age=31536000");
						} else {
							response.header("Cache-Control","no-cache");
						}
						return response;
					});
				}

			} else {
				return chain.proceed(request);
			}
		});
	}

	private boolean isDigestVersion(String uri) {
		String manifestPath = uri;
		Properties manifest = AssetPipelineConfigHolder.manifest;
		return manifest.getProperty(manifestPath,null) != null ? false : true;
	}

	private String getCurrentETag(String uri) {
		String manifestPath = uri;
		Properties manifest = AssetPipelineConfigHolder.manifest;
		return "\"" + (manifest.getProperty(manifestPath,manifestPath)) + "\"";
	}


	private AssetAttributes resolveAssetAttribute(String filename) {
		URL assetUrl = this.getClass().getClassLoader().getResource("assets/" + filename);
		URL gzipAsset = this.getClass().getClassLoader().getResource("assets/" + filename + ".gz");
		if(assetUrl == null) {
			assetUrl = this.getClass().getClassLoader().getResource("assets/" + filename + "/index.html");
			gzipAsset = this.getClass().getClassLoader().getResource("assets/" + filename + "/index.html.gz");
		}
		AssetAttributes attribute = new AssetAttributes(assetUrl != null, gzipAsset != null, false,0L,0L,null,assetUrl,gzipAsset);
		fileCache.put(filename,attribute);
		return attribute;
	}


	private static boolean shouldCompile(HttpRequest<?> request) {
		return !"false".equals(request.getParameters().getFirst(COMPILE_PARAM));
	}



}

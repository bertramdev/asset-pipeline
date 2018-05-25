package asset.pipeline.micronaut;


import io.micronaut.context.annotation.Value;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.server.types.files.StreamedFile;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;
import org.slf4j.*;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.Optional;

@Filter("/${assets.mapping:}/**")
public class AssetPipelineFilter implements HttpServerFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AssetPipelineFilter.class);
	AssetPipelineService assetPipelineService;

	@Value("${assets.mapping:}")
	protected String assetMapping;

	@Inject
	public AssetPipelineFilter(AssetPipelineService assetPipelineService) {
		this.assetPipelineService = assetPipelineService;
	}


	/**
	 * Variation of the {@link #doFilter(HttpRequest, FilterChain)} method that accepts a {@link ServerFilterChain}
	 * which allows to mutate the outgoing HTTP response.
	 *
	 * @param request The request
	 * @param chain   The chain
	 * @return A {@link Publisher} that emits a {@link MutableHttpResponse}
	 * @see #doFilter(HttpRequest, FilterChain)
	 */
	@Override
	public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {

		final String encoding = request.getParameters().getFirst("encoding").orElseGet(() -> request.getCharacterEncoding().toString());




		String fileUri = request.getUri().getPath();
		final String baseAssetUrl = "/" + assetMapping;

		if(fileUri.startsWith(baseAssetUrl)) {
			fileUri = fileUri.substring(baseAssetUrl.length());
		}
		final MediaType contentType = MediaType.forExtension(NameUtils.extension(fileUri)).orElseGet(() -> null);
		final String format =  contentType != null ? contentType.toString() : null;
		LOG.debug("Loading Asset For URL: " + fileUri);
		if(assetPipelineService.isDevMode()) {
			return assetPipelineService.handleAssetDevMode(fileUri,format,encoding,request).switchMap( contents -> {
				if(contents.isPresent()) {
					return Flowable.fromCallable(() -> {
						MutableHttpResponse<byte[]> response = HttpResponse.ok(contents.get());
						response.header("Cache-Control", "no-cache, no-store, must-revalidate");
						response.header("Pragma","no-cache");
						response.header("Expires","0");
						MediaType responseContentType = contentType != null ? contentType : MediaType.forExtension("html").get();
						response.contentType(responseContentType);
						response.contentLength(contents.get().length);

						return response;
					});
				} else {
					return chain.proceed(request);
				}
			});
		} else {
			MediaType requestContentType = contentType != null ? contentType : MediaType.forExtension("html").get();
			return assetPipelineService.handleAsset(fileUri,requestContentType,encoding,request,chain);
		}



	}
}
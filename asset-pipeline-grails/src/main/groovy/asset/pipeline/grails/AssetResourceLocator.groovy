package asset.pipeline.grails


import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.DirectiveProcessor
import asset.pipeline.GenericAssetFile
import org.grails.core.io.DefaultResourceLocator
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource


class AssetResourceLocator extends DefaultResourceLocator {

	@Override
	Resource findResourceForURI(final String uri) {
		Resource resource = super.findResourceForURI(uri)
		if(!resource) {
			resource = findAssetForURI(uri)
		}
		return resource
	}

	Resource findAssetForURI(String uri) {
		final Resource resource
		final Properties manifest = AssetPipelineConfigHolder.manifest
		if(manifest) {
			uri = manifest?.getProperty(uri, uri)

			final String assetUri = "assets/${uri}"
			Resource defaultResource = defaultResourceLoader.getResource(assetUri)
			if (!defaultResource?.exists()) {
				defaultResource = defaultResourceLoader.getResource("classpath:${assetUri}")
			}
			if (defaultResource?.exists()) {
				resource = defaultResource
			}
		} else {
			final List<String> contentTypes = AssetHelper.assetMimeTypeForURI(uri)
			final String       contentType  = contentTypes ? contentTypes[0] : null
			final String       extension    = AssetHelper.extensionFromURI(uri)
			final String       name         = AssetHelper.nameWithoutExtension(uri)
			final AssetFile    assetFile    = AssetHelper.fileForUri(name, contentType, extension)
			if(assetFile) {
				if(assetFile instanceof GenericAssetFile) {
					resource = new ByteArrayResource(assetFile.bytes)
				} else {
					final DirectiveProcessor directiveProcessor = new DirectiveProcessor(contentType, null, this.class.classLoader)
					final def fileContents = directiveProcessor.compile(assetFile)
					final String encoding = assetFile.encoding
					if(encoding) {
						resource = new ByteArrayResource(fileContents.getBytes(encoding))
					} else {
						resource = new ByteArrayResource(fileContents.bytes)
					}
				}
			}
		}
		return resource
	}
}

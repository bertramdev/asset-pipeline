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
	Resource findResourceForURI(String uri) {
		Resource resource = super.findResourceForURI(uri)
		if(!resource) {
			resource = findAssetForURI(uri)
		}
		return resource
	}

	Resource findAssetForURI(String uri) {
		Resource resource
		if(warDeployed) {
			Properties manifest = AssetPipelineConfigHolder.manifest
			uri = manifest?.getProperty(uri, uri)

			String assetUri = "assets/${uri}"
			Resource defaultResource = defaultResourceLoader.getResource(assetUri)
			if (!defaultResource?.exists()) {
				defaultResource = defaultResourceLoader.getResource("classpath:${assetUri}")
			}
			if (defaultResource?.exists()) {
				resource = defaultResource
			}
		} else {
			List<String> contentTypes = AssetHelper.assetMimeTypeForURI(uri)
			String       contentType  = contentTypes ? contentTypes[0] : null
			String       extension    = AssetHelper.extensionFromURI(uri)
			String       name         = AssetHelper.nameWithoutExtension(uri)
			AssetFile    assetFile    = AssetHelper.fileForUri(name, contentType, extension)
			if(assetFile) {
				if(assetFile instanceof GenericAssetFile) {
					resource = new ByteArrayResource(assetFile.bytes)
				} else {
					DirectiveProcessor directiveProcessor = new DirectiveProcessor(contentType, null, this.class.classLoader)
					def fileContents = directiveProcessor.compile(assetFile)
					String encoding = assetFile.encoding
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

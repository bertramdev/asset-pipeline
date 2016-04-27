package asset.pipeline.grails.fs

import org.springframework.core.io.*

class SpringResourceAssetResolverSpec extends spock.lang.Specification {

    def "Test that the getAsset method resolves an AssetFile instance"() {
        given:"A resourceLoader instancer with an asset resolver"
            def resourceLoader = new DefaultResourceLoader()
            def assetResolver = new SpringResourceAssetResolver("classpath", resourceLoader, "META-INF/assets/javascripts")

        when:"A resource is loaded"
            def assetFile = assetResolver.getAsset("test", "application/javascript", "js")


        then:"It resolves the asset correctly"
            assetFile != null
            assetFile.path == 'test.js'
            assetFile.inputStream != null

    }

    def "Test that the getAssets method resolves all AssetFile instances"() {
        given:"A resourceLoader instancer with an asset resolver"
            def resourceLoader = new DefaultResourceLoader()
            def assetResolver = new SpringResourceAssetResolver("classpath", resourceLoader, "META-INF/assets/javascripts")

        when:"A resource is loaded"
            def assetFiles = assetResolver.getAssets("/", "application/javascript")


        then:"It resolves the asset correctly"
            assetFiles.size() == 1

    }

    def "Test that the scanForResources method scans and locates AssetFile instances"() {
        given:"A resourceLoader instancer with an asset resolver"
            def resourceLoader = new DefaultResourceLoader()
            def assetResolver = new SpringResourceAssetResolver("classpath", resourceLoader, "META-INF/assets/javascripts")

        when:"A resource is loaded"
            def assetFiles = assetResolver.scanForFiles([], [ '*'])

        then:"It resolves the asset correctly"
            assetFiles.size() == 1
    }

}
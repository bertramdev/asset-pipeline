package asset.pipeline.grails.fs

import asset.pipeline.JsEs6AssetFile
import org.springframework.core.io.DefaultResourceLoader

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
            assetFiles.size() == 2

    }

    def "Test that the scanForResources method scans and locates AssetFile instances"() {
        given:"A resourceLoader instancer with an asset resolver"
            def resourceLoader = new DefaultResourceLoader()
            def assetResolver = new SpringResourceAssetResolver("classpath", resourceLoader, "META-INF/assets/javascripts")

        when:"A resource is loaded"
            def assetFiles = assetResolver.scanForFiles([], [ '*'])

        then:"It resolves the asset correctly"
            assetFiles.size() == 2
    }

    void "should prefer .js file over .mjs file when explicitly requesting .js extension"() {
        given:
        def resourceLoader = new DefaultResourceLoader()
        def resolver = new SpringResourceAssetResolver("classpath", resourceLoader, "META-INF/assets/javascripts")

        when:
        def file = resolver.getAsset('test.js', 'application/javascript')

        then:
        file != null
        file.name == 'test.js'
        file.path.endsWith('.js')
    }

    void "should prefer .mjs file over .js file when explicitly requesting .mjs extension"() {
        given:
        def resourceLoader = new DefaultResourceLoader()
        def resolver = new SpringResourceAssetResolver("classpath", resourceLoader, "META-INF/assets/javascripts")

        when:
        def file = resolver.getAsset('test.mjs', 'application/javascript')

        then:
        file != null
        file.name == 'test.mjs'
        file.path.endsWith('.mjs')
    }

    void "should prefer .mjs file over .js when no extension is specified"() {
        given:
        def resourceLoader = new DefaultResourceLoader()
        def resolver = new SpringResourceAssetResolver("classpath", resourceLoader, "META-INF/assets/javascripts")

        when:
        def jsFile = resolver.getAsset('test', 'application/javascript')

        then:
        jsFile != null
        jsFile instanceof JsEs6AssetFile
        jsFile.name == 'test.mjs'
        jsFile.path.endsWith('.mjs')
    }
}
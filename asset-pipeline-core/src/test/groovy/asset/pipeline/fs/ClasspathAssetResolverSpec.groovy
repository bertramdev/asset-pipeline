package asset.pipeline.fs

import asset.pipeline.CssAssetFile
import asset.pipeline.GenericAssetFile
import asset.pipeline.JsAssetFile
import spock.lang.Specification

/**
 * Created by davydotcom on 4/21/16.
 */
class ClasspathAssetResolverSpec extends Specification {
    void "should be able to fetch generic files with seperated extension"() {
        given:
        def resolver = new ClasspathAssetResolver('application','META-INF/assets','META-INF/assets.list')
        when:
        def file = resolver.getAsset('asset-test/grails_logo',null,'png')
        then:
        file instanceof GenericAssetFile
    }

    void "should not resolve an asset if no extension or content type is specified"() {
        given:
        def resolver = new ClasspathAssetResolver('application','META-INF/assets','META-INF/assets.list')
        when:
        def file = resolver.getAsset('asset-test/lib')
        then:
        file == null
    }

    void "should be able to fetch generic files without seperated extension"() {
        given:
        def resolver = new ClasspathAssetResolver('application','META-INF/assets','META-INF/assets.list')
        when:
        def file = resolver.getAsset('asset-test/grails_logo.png')
        then:
        file instanceof GenericAssetFile
    }

    void "should be able to resolve js files based on a content-type"() {
        given:
        def resolver = new ClasspathAssetResolver('application','META-INF/assets','META-INF/assets.list')
        when:
        def file = resolver.getAsset('asset-test/lib','application/javascript')
        then:
        file instanceof JsAssetFile
    }

    void "should be able to resolve css files based on a content-type"() {
        given:
        def resolver = new ClasspathAssetResolver('application','META-INF/assets','META-INF/assets.list')
        when:
        def file = resolver.getAsset('asset-test/nested/filec','text/css')
        then:
        file instanceof CssAssetFile
    }

    void "should be able to fetch files recursively by content-type"() {
        given:
        def resolver = new ClasspathAssetResolver('application','META-INF/assets','META-INF/assets.list')
        when:
        def files = resolver.getAssets('asset-test','application/javascript')
        then:
        files?.size() == 3
    }

    void "should be able to fetch files recursively by content-type with relative baseFile"() {
        given:
        def resolver = new ClasspathAssetResolver('application','META-INF/assets','META-INF/assets.list')
        when:
        def relativeFile = resolver.getAsset('asset-test/lib','application/javascript')
        println "Fetched Relative File ${relativeFile?.name}"
        def files = resolver.getAssets('.','application/javascript', null, true, relativeFile)
        then:
        files?.size() == 3
    }
}

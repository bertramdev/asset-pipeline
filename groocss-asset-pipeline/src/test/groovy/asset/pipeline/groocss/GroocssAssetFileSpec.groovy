package asset.pipeline.groocss

import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import asset.pipeline.AssetHelper
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.fs.FileSystemAssetResolver
import spock.lang.Specification

class GroocssAssetFileSpec extends Specification {
    void setup() {
        AssetPipelineConfigHolder.resolvers = []
        AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver('simple','src/test/resources', false))
    }

    void cleanup() {
        AssetPipelineConfigHolder.resolvers = []
    }

    void 'applying compiler to simple groocss asset file succeeds'() {
        given:
        AssetFile assetFile = AssetHelper.fileForFullName('simple.css.groovy')

        when:
        String css = assetFile.processedStream(new AssetCompiler())

        then:
        css == ".border-1{border: 1px;}"
    }

    void 'applying compiler to nested type groocss asset file succeeds'() {
        given:
        AssetFile assetFile = AssetHelper.fileForFullName('nested.css.groovy')

        when:
        String css = assetFile.processedStream(new AssetCompiler())

        then:
        css == '.logo-wrapper > img{width: 15em;}\n.logo-wrapper{width: 50%;\n\tdisplay: flex;\n\tjustify-content: center;\n\talign-items: center;\n\topacity: 60%;}'
    }

    void 'applying compiler to asset file with measures succeeds'() {
        given:
        AssetFile assetFile = AssetHelper.fileForFullName('measures.css.groovy')

        when:
        String css = assetFile.processedStream(new AssetCompiler())

        then:
        css == '.border-1{border: 1px;}\n.border-2{border: 2em;}'
    }
}

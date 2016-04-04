import static ratpack.groovy.Groovy.ratpack
import asset.pipeline.ratpack.AssetPipelineModule


ratpack {
  bindings {
    module(AssetPipelineModule) { cfg ->
    	cfg.url("/")
    	cfg.sourcePath("../../../src/assets")
    }
  }

  handlers {
  	get("test") {
  		render "Test Me"
  	}
  }
}

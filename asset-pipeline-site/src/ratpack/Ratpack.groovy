import asset.pipeline.ratpack.site.PluginConfig
import groovy.json.JsonSlurper
import ratpack.config.ConfigData
import ratpack.func.Action
import ratpack.http.client.HttpClient
import ratpack.http.client.RequestSpec

import static ratpack.groovy.Groovy.ratpack
import asset.pipeline.ratpack.AssetPipelineModule
import ratpack.handlebars.HandlebarsModule
import static ratpack.handlebars.Template.handlebarsTemplate;


ratpack {
	bindings {
		def configData = ConfigData.of { d ->
			d.sysProps()
				.env()
				.build()
		}
		bindInstance(configData.get(PluginConfig))
		module(AssetPipelineModule) { cfg ->
			cfg.url("/")
			cfg.sourcePath("../../../src/assets")
		}
		module(HandlebarsModule)
	}

	handlers {
		get("plugins") { ctx ->
			PluginConfig pluginConfig = ctx.get(PluginConfig.class)

			String phrase = ctx.getRequest().getQueryParams().get("phrase")
			String uriString = "search/packages?subject=bertramlabs&repo=asset-pipeline"
			if(phrase) {
				uriString += "&name=${phrase}"
			}
			HttpClient client = ctx.get(HttpClient.class)
			URI uri = new URI("https://api.bintray.com/${uriString}")
			client.get(uri, { action ->
				action.basicAuth(pluginConfig.bintrayUsername, pluginConfig.bintrayApiKey)
			}).then { response ->
				def resultSet = new JsonSlurper().parseText(response.getBody().getText())
				def plugins = resultSet?.collect { result ->
					def map = [
						owner            : result.owner,
						name             : result.name,
						desc             : result.desc,
						rating           : result.rating,
						latest_version   : result.latest_version,
						vcs_url          : result.vcs_url,
						issue_tracker_url: result.issue_tracker_url,
						updated          : result.updated,
						system_id        : result.system_ids ? result.system_ids[0] : null,
						plugin_url       : "https://www.bintray.com/${result.owner}/${result.repo}/${result.name}"
					]
					if(result.website_url) {
						map.website_url = result.website_url
					}
					return map
				}
				def pluginTemplate = handlebarsTemplate("plugins.html") {
					it.put("plugins", plugins)
					it.put("phrase", phrase)
				}
				ctx.render(pluginTemplate)
			}
		}
	}
}

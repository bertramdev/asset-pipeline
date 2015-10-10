/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package asset.pipeline.ratpack

import ratpack.func.Action
import ratpack.guice.Guice
import ratpack.server.BaseDir
import ratpack.server.RatpackServerSpec
import ratpack.server.ServerConfig
import ratpack.test.embed.EmbeddedApp
import spock.lang.AutoCleanup
import spock.lang.Specification

import static asset.pipeline.ratpack.TestConstants.BASE_DIR

class ConfigurationFunctionalSpec extends Specification {
  private static final String RECONFIGURED_URL = "static"

  @AutoCleanup
  @Delegate
  EmbeddedApp app = of({ spec -> spec
      .serverConfig(ServerConfig.embedded().baseDir(BASE_DIR.toAbsolutePath()))
      .registry(Guice.registry { b -> b
          .module(AssetPipelineModule, { config ->
            config.url(RECONFIGURED_URL+'/').indexFile("default.htm")
          } as Action<AssetPipelineModule.Config>)
      })
      .handlers { }
  } as Action<RatpackServerSpec>)

  void "should serve content from the configured url"() {
    given:
    def response = httpClient.get("${RECONFIGURED_URL}/index.html")

    expect:
    response.statusCode == 200
    response.body.text.trim() == BASE_DIR.resolve("../assets/html/index.html").text
  }

  void "should serve index file from configured filename"() {
    given:
    def response = httpClient.get(RECONFIGURED_URL)

    expect:
    response.statusCode == 200
    response.body.text.trim() == BASE_DIR.resolve("../assets/html/default.htm").text
  }
}

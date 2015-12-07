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
import ratpack.path.PathBinder
import ratpack.path.PathBinding
import ratpack.path.internal.PathBindingStorage
import ratpack.server.RatpackServerSpec
import ratpack.server.ServerConfig
import ratpack.test.embed.EmbeddedApp
import spock.lang.AutoCleanup
import spock.lang.Specification

import static asset.pipeline.ratpack.TestConstants.BASE_DIR

class PathBindingFunctionalSpec extends Specification {

    private static final String PATH = "additional-path"

    @AutoCleanup
    @Delegate
    EmbeddedApp app = of({ spec ->
        spec
            .serverConfig(ServerConfig.embedded().baseDir(BASE_DIR.toAbsolutePath()))
            .registry(Guice.registry { it.module(AssetPipelineModule) })
            .handlers {
            it.all { ctx ->
                def binding = PathBinder.parse(PATH, false).bind(ctx.get(PathBinding))

                binding.ifPresent {
                    ctx.get(PathBindingStorage).push(it)
                }

                ctx.next()
            }
        }
    } as Action<RatpackServerSpec>)

    void "should take current path binding into consideration when serving assets"() {
        given:
        def response = httpClient.get("${PATH}/assets/index.html")

        expect:
        response.statusCode == 200
        response.body.text.trim() == BASE_DIR.resolve("../assets/html/index.html").text
    }

}

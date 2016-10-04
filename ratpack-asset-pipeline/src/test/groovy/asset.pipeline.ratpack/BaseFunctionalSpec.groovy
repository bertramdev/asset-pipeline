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

import ratpack.error.ClientErrorHandler
import ratpack.func.Action
import ratpack.guice.Guice
import ratpack.handling.Context
import ratpack.server.RatpackServerSpec
import ratpack.server.ServerConfig
import ratpack.test.embed.EmbeddedApp
import spock.lang.AutoCleanup
import spock.lang.Specification

import static asset.pipeline.ratpack.TestConstants.BASE_DIR

class BaseFunctionalSpec extends Specification {

  @AutoCleanup
  @Delegate
  EmbeddedApp app = of({ spec -> spec
    .serverConfig(ServerConfig.embedded().baseDir(BASE_DIR.toAbsolutePath()))
    .registry(Guice.registry { b -> b
      .module(AssetPipelineModule)
      .module({
        it.bind(ClientErrorHandler).toInstance({ Context context, int statusCode ->
          context.response.status(404).send("from error handler")
        } as ClientErrorHandler)
      })
    })
    .handlers { c -> c
      .get { ctx -> ctx.render("base") }
      .path("foo") { ctx -> ctx.render("foo") }
    }
  } as Action<RatpackServerSpec>)

  void "should serve assets"() {
    given:
    def response = httpClient.get("/assets/index.html")

    expect:
    response.statusCode == 200
    response.body.text.trim() == BASE_DIR.resolve("../assets/html/index.html").text
  }

  void "should serve index.html for directory path"() {
    given:
    def response = httpClient.get("assets")

    expect:
    response.statusCode == 200
    response.body.text.trim() == BASE_DIR.resolve("../assets/html/index.html").text
  }

  void "other handlers should remain unaffected by AP"() {
    when:
    def response = httpClient.get()

    then:
    response.statusCode == 200
    response.body.text == "base"

    when:
    response = httpClient.get("foo")

    then:
    response.statusCode == 200
    response.body.text == "foo"
  }

  void "client error handler should be delegated to when requesting a non existing path"() {
    when:
    def response = httpClient.get("not-existing-path")

    then:
    response.statusCode == 404
    response.body.text == "from error handler"
  }

  void "requesting empty urls should not cause server errors"() {
    expect:
    statusCodeForExactPath("///") != 500
  }

  private int statusCodeForExactPath(String path) {
    def statusLine = new Socket("localhost", address.port).withCloseable { socket ->
      def pw = new PrintWriter(socket.outputStream)
      pw.println("GET $path HTTP/1.1")
      pw.println()
      pw.flush()
      new InputStreamReader(socket.inputStream).readLine()
    }
    statusLine.tokenize()[1].toInteger()
  }
}

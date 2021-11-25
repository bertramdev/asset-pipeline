/*
 * Copyright 2016 the original author or authors.
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
package asset.pipeline.dart

import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import com.caoccao.javet.enums.JSRuntimeType
import com.caoccao.javet.interception.logging.JavetStandardConsoleInterceptor
import com.caoccao.javet.interop.V8Runtime
import com.caoccao.javet.interop.engine.IJavetEngine
import com.caoccao.javet.interop.engine.IJavetEnginePool
import com.caoccao.javet.interop.engine.JavetEnginePool
import com.caoccao.javet.values.reference.V8ValueObject
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class SassProcessor extends AbstractProcessor {
    final IJavetEnginePool<V8Runtime> javetEnginePool
    final String sassCompiler

    SassProcessor(AssetCompiler precompiler) {
        super(precompiler)

        // Load script from classpath
        URL resource = getClass().classLoader.getResource("js/compiler.js")
        sassCompiler = resource.openStream().text

        // Setup a Javet engine for pooling
        javetEnginePool = new JavetEnginePool<>()
        javetEnginePool.getConfig().setJSRuntimeType(JSRuntimeType.Node)
        javetEnginePool.getConfig().setAllowEval(true)
    }

    /**
     * Called whenever the asset pipeline detects a change in the file provided as argument
     * @param input the content of the SCSS file to compile
     * @param assetFile
     * @return the compiled output
     */
    String process(String input, AssetFile assetFile) {
        log.debug "Compiling $assetFile.path"
        println "Compiling $assetFile.path"

        String output = null

        IJavetEngine<V8Runtime> javetEngine = javetEnginePool.getEngine()
        try {
            V8Runtime v8Runtime = javetEngine.getV8Runtime()

            // Create a Javet console interceptor.
            JavetStandardConsoleInterceptor javetConsoleInterceptor = new JavetStandardConsoleInterceptor(v8Runtime)
            javetConsoleInterceptor.register(v8Runtime.getGlobalObject())

            // Bind the importer callback
            SassAssetFileLoader loader = new SassAssetFileLoader(assetFile)
            V8ValueObject v8ValueObject = v8Runtime.createV8ValueObject()
            try {
                v8Runtime.getGlobalObject().set("importer", v8ValueObject)
                v8ValueObject.bind(loader)
            }
            finally {
                v8ValueObject.close()
            }

            // Setup the options passed to the SASS compiler
            // https://sass-lang.com/documentation/js-api/interfaces/LegacyStringOptions
            v8Runtime.getGlobalObject().setProperty("compileOptions", [
                assetFilePath: assetFile.path,
                data: input,
            ])

            // Compile and retrieve the CSS output
            v8Runtime.getExecutor(sassCompiler).executeVoid()
            output = v8Runtime.getGlobalObject().get("css") as String

            // Unregister the Javet console to V8 global object.
            javetConsoleInterceptor.unregister(v8Runtime.getGlobalObject())
            v8Runtime.lowMemoryNotification()
        }
        finally {
            javetEngine.close()
        }

        output
    }
}

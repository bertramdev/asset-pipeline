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
import asset.pipeline.AssetPipelineConfigHolder
import com.caoccao.javet.enums.JSRuntimeType
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.V8Host
import com.caoccao.javet.interop.loader.IJavetLibLoadingListener
import com.caoccao.javet.interop.loader.JavetLibLoader
import com.caoccao.javet.values.reference.V8ValueObject
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class SassProcessor extends AbstractProcessor {
    final String sassCompiler

    // Compiler options
    final Map configOptions = (AssetPipelineConfigHolder.config?.sass ?: [:]) as Map

    static {
        File nativeLibrary = new NativeLibraryLoader(JSRuntimeType.Node).extractNativeLibrary()

        // Override Javet to use the library that we downloaded for this platform
        JavetLibLoader.setLibLoadingListener(new IJavetLibLoadingListener() {
            @Override
            File getLibPath(JSRuntimeType jsRuntimeType) {
                return nativeLibrary.parentFile
            }

            @Override
            boolean isDeploy(JSRuntimeType jsRuntimeType) {
                return false
            }

            @Override
            boolean isSuppressingError(JSRuntimeType jsRuntimeType) {
                return true
            }
        })
    }

    SassProcessor(AssetCompiler precompiler) {
        super(precompiler)

        // Load script from classpath
        URL resource = getClass().classLoader.getResource("js/compiler.js")
        sassCompiler = resource.openStream().text
    }

    /**
     * Called whenever the asset pipeline detects a change in the file provided as argument
     * @param input the content of the SCSS file to compile
     * @param assetFile
     * @return the compiled output
     */
    String process(String input, AssetFile assetFile) {
        log.debug "Compiling $assetFile.path"

        String output = null

        NodeRuntime nodeRuntime = V8Host.getNodeInstance().createV8Runtime(true, JSRuntimeType.Node.runtimeOptions) as NodeRuntime
        try {
            nodeRuntime.allowEval(true)

            // Bind the importer callback
            SassAssetFileLoader loader = new SassAssetFileLoader(assetFile)

            V8ValueObject v8ValueObject = nodeRuntime.createV8ValueObject()
            try {
                nodeRuntime.getGlobalObject().set("importer", v8ValueObject)
                v8ValueObject.bind(loader)
            }
            finally {
                v8ValueObject.close()
            }

            // Combine options
            Map compileOptions = configOptions + [data: input]

            // Setup the options passed to the SASS compiler
            // https://sass-lang.com/documentation/js-api/interfaces/LegacyStringOptions
            nodeRuntime.getGlobalObject().setProperty("compileOptions", compileOptions)

            // Compile and retrieve the CSS output
            nodeRuntime.getExecutor(sassCompiler).executeVoid()
            output = nodeRuntime.getGlobalObject().get("css") as String

            // Cleanup the global importer
            nodeRuntime.getGlobalObject().delete("importer")
        }
        finally {
            nodeRuntime.close()
        }

        output
    }
}

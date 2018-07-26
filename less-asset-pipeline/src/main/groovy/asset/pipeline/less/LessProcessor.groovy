package asset.pipeline.less

import asset.pipeline.AssetHelper
import asset.pipeline.CacheManager
import asset.pipeline.AbstractProcessor
import asset.pipeline.AssetCompiler
import asset.pipeline.AssetFile
import groovy.util.logging.Commons
import asset.pipeline.processors.CssProcessor
import org.mozilla.javascript.Context
import org.mozilla.javascript.JavaScriptException
import org.mozilla.javascript.NativeArray
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.Scriptable
import sun.net.www.protocol.asset.Handler

@Commons
class LessProcessor extends AbstractProcessor {
    public static final java.lang.ThreadLocal threadLocal   = new ThreadLocal();
    public static final java.lang.ThreadLocal localCompiler = new ThreadLocal();
    public static final java.lang.ThreadLocal resultsMap    = new ThreadLocal();
    Scriptable globalScope
    ClassLoader classLoader


    LessProcessor(AssetCompiler precompiler) {
        super(precompiler)
        try {
            classLoader = getClass().getClassLoader()

            def shellJsResource = classLoader.getResource('asset/pipeline/less/shell.js')
            def envRhinoJsResource = classLoader.getResource('asset/pipeline/less/env.rhino.js')
            def hooksJsResource = classLoader.getResource('asset/pipeline/less/hooks.js')
            def lessJsResource = classLoader.getResource('asset/pipeline/less/less-2.5.0.js')
            def compileJsResource = classLoader.getResource('asset/pipeline/less/compile.js')
            Context cx = Context.enter()

            cx.setOptimizationLevel(-1)
            globalScope = cx.initStandardObjects()
            this.evaluateJavascript(cx, shellJsResource)
            this.evaluateJavascript(cx, envRhinoJsResource)
            this.evaluateJavascript(cx, hooksJsResource)
            this.evaluateJavascript(cx, lessJsResource)
            this.evaluateJavascript(cx, compileJsResource)

        } catch (Exception e) {
            throw new Exception("LESS Engine initialization failed.", e)
        } finally {
            try {
                Context.exit()
            } catch (IllegalStateException e) {
            }
        }
    }

    def evaluateJavascript(context, resource) {
        // def inputStream = resource.inputStream
        context.evaluateString globalScope, resource.getText('UTF-8'), resource.file, 1, null

    }

    String process(String input, AssetFile assetFile) {
        try {
            threadLocal.set(assetFile);
            localCompiler.set(precompiler)
            resultsMap.set(null);

            def cx = Context.enter()
            def compileScope = cx.newObject(globalScope)
            compileScope.setParentScope(globalScope)
            compileScope.put("lessSrc", compileScope, input)

            cx.evaluateString(compileScope, "compile(lessSrc, ['assets'])", "LESS compile command", 0, null)
            while(resultsMap.get() == null) {
                Thread.sleep(5)
            }

            def results = resultsMap.get();
            if(!results.get('success')) {
                println "Error Processing Results ${results.get('error')}";
                return ''
                // throw new Exception('Less Compiler Failed to Return Successfully.')
            } else {
                return results.get('css').toString();
            }
            // return result.toString()
        } catch (JavaScriptException e) {
            org.mozilla.javascript.NativeObject errorMeta = (org.mozilla.javascript.NativeObject) e.value

            def errorDetails = "LESS Engine Compiler Failed - ${assetFile.path}.\n"
            if (precompiler) {
                errorDetails += "**Did you mean to compile this file individually (check docs on exclusion)?**\n"
            }
            if (errorMeta && errorMeta.get('message')) {

                errorDetails += " -- ${errorMeta.get('message')} Near Line: ${errorMeta.line}, Column: ${errorMeta.column}\n"
            }
            if (errorMeta != null && errorMeta.get('extract') != null) {
                List extractArray = (org.mozilla.javascript.NativeArray) errorMeta.get('extract')
                errorDetails += "    --------------------------------------------\n"
                extractArray.each { error ->
                    errorDetails += "    ${error}\n"
                }
                errorDetails += "    --------------------------------------------\n\n"
            }

            log.error(errorDetails)

         } catch (Exception e) {
             throw new Exception("""
         LESS Engine compilation of LESS to CSS failed.
         $e
         """)
        } finally {
            Context.exit()
        }
        return input
    }

    static void print(text) {
        println text
    }

    static void error(text) {
        log.error('LESS Compile Error: ' + text);
    }

    static URL getURL(String uri) {
        return new java.net.URL(null,uri, new Handler());
    }

    static void setResults(NativeObject resultObject) {
        resultsMap.set(resultObject);
    }

    static String contentForURL(String text, URL url) {
        def assetFile = threadLocal.get();
        def precompiler = localCompiler.get();
        def baseFile = assetFile?.baseFile ?: assetFile
        AssetFile newFile = AssetHelper.fileForUri( url.path, null, null, baseFile)
        if(newFile) {
            def cssProcessor = new CssProcessor(precompiler)
            return cssProcessor.process(text, newFile)
        } else {
            return null;            
        }
    }

    static String resolveUri(String path, NativeArray paths) {
        try {
            def fileName = AssetHelper.nameWithoutExtension(path)
            def assetFile = threadLocal.get();
            def baseFile = assetFile?.baseFile ?: assetFile
            def newFile
            if( fileName.startsWith( AssetHelper.DIRECTIVE_FILE_SEPARATOR ) ) {
                newFile = AssetHelper.fileForUri( fileName, 'text/css', null, assetFile?.baseFile )
            }
            else {
                def relativeFileName = [ assetFile.parentPath, fileName ].join( AssetHelper.DIRECTIVE_FILE_SEPARATOR )
                newFile = AssetHelper.fileForUri( relativeFileName, 'text/css', null, assetFile?.baseFile )
            }

            if( !newFile && !fileName.startsWith( AssetHelper.DIRECTIVE_FILE_SEPARATOR ) ) {
                newFile = AssetHelper.fileForUri( AssetHelper.DIRECTIVE_FILE_SEPARATOR + fileName, 'text/css', null, assetFile?.baseFile )
            }
            else if (!newFile) {
                log.warn( "Unable to Locate Asset: ${ fileName }" )
            }

            if (newFile) {
                CacheManager.addCacheDependency(assetFile.path, newFile)
                return "asset:///${newFile.path}".toString()
            }
            return null
        } catch(e) {
            log.error("Error Resolving URI For LESS Engine",e)
            return null
        }

    }

}

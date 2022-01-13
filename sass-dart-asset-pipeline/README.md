SASS Asset Pipeline
==========================
The `sass-dart-asset-pipeline` is a plugin that provides SASS / SCSS support for the asset-pipeline static asset management plugin using the [dart-sass](https://sass-lang.com/dart-sass) native JS compiler.

The plugin uses [Javet](https://www.caoccao.com/Javet/) (based on Google's V8 runtime) to allow for execution of the dart-sass JS compiler directly from Java.

For more information on how to use asset-pipeline, visit [here](http://www.github.com/bertramdev/asset-pipeline).

Integration
===========
Javet requires native libraries for integration. The first time you execute the pipeline, the appropriate native library will be downloaded for your platform. The supported
platforms are:

* Linux - x86_64
* Windows - x86_64
* MacOS - x86_64, arm64

The platform JAR is downloaded by default from Maven Central to the Java system temp directory. You can
override this behavior by setting the following options:

* javetBaseUrl = "https://repo1.maven.org/maven2/com/caoccao/javet" (default)
* javetLibraryHome = System.getProperty('java.io.tmpdir') (default)

If the native library already exists at `javetLibraryHome` it will not be downloaded again.

The full platform URL is constructed from the `javetBaseUrl`. 

For example, on Mac OS, it would look like this:

`"${javetBaseUrl}/javet-macos/1.0.6/javet-macos-1.0.6.jar"`

Configuration
-------------
Configuration is passed through to the dart-sass compiler:

https://sass-lang.com/documentation/js-api/interfaces/LegacyStringOptions

Common options are:

* sass.outputStyle = "expanded" | "compressed" (default: "expanded")
* sass.indentType = "space" | "tab" (default "space")
* sass.indentWidth = 0..10 (default: 2)
* sass.quietDeps = boolean (default: false) - Don't report deprecation warnings for imported files

For example:
```
assets {
    configOptions = [
        javetBaseUrl: 'https://myrepo.mycompany.com/repository/com/caoccao/javet'
        sass: [
            quietDeps: true, 
            outputStyle: 'compressed'
        ]
    ]
}
```

Usage
-----
Simply create `scss` or `sass` files in your assets folder.

Things to be done
-----------------
* Find a way to generate proper source maps with full paths

SASS Asset Pipeline
==========================
The `sass-dart-asset-pipeline` is a plugin that provides SASS / SCSS support for the asset-pipeline static asset management plugin using the [dart-sass](https://sass-lang.com/dart-sass) native JS compiler.

The plugin uses [Javet](https://www.caoccao.com/Javet/) (based on Google's V8 runtime) to allow for execution of the dart-sass JS compiler directly from Java.

For more information on how to use asset-pipeline, visit [here](http://www.github.com/wondrify/asset-pipeline).

Integration
===========
Javet requires native libraries for integration. The first time you execute the pipeline, the appropriate native library will be downloaded for your platform. The supported
platforms are:

* Linux - x86_64, arm64
* Windows - x86_64
* MacOS - x86_64, arm64

The platform JAR is downloaded by default from Maven Central to the Java system temp directory. You can
override this behavior by setting the following options:

* javetBaseUrl = "https://repo1.maven.org/maven2/com/caoccao/javet" (default)
* javetLibraryHome = System.getProperty('java.io.tmpdir') (default)

If the native library already exists at `javetLibraryHome` it will not be downloaded again.

The full platform URL is constructed from the `javetBaseUrl` using the artifact naming scheme
`javet-{runtime}-{os}-{arch}`.

For example, on Apple Silicon (arm64), it would look like this:

`"${javetBaseUrl}/javet-node-macos-arm64/5.0.5/javet-node-macos-arm64-5.0.5.jar"`

Configuration
-------------
Configuration is passed through to the dart-sass compiler using the modern JS API:

https://sass-lang.com/documentation/js-api/functions/compilestring/

Common options are:

* sass.style = "expanded" | "compressed" (default: "expanded"). The legacy `outputStyle` key is also accepted for backward compatibility.
* sass.quietDeps = boolean (default: false) - Don't report deprecation warnings for imported files
* sass.silenceDeprecations = list of deprecation IDs to silence (e.g. `['import']`)

For example:
```
assets {
    configOptions = [
        javetBaseUrl: 'https://myrepo.mycompany.com/repository/com/caoccao/javet'
        sass: [
            quietDeps: true,
            style: 'compressed'
        ]
    ]
}
```

Usage
-----
Simply create `scss` or `sass` files in your assets folder.

Upgrading to 5.x
-----------------

This version upgrades from the dart-sass legacy JS API to the modern JS API (`compileString`).
If you are upgrading from a prior version, be aware of the following breaking changes:

### Configuration changes

* `outputStyle` has been renamed to `style`. If you have `outputStyle` in your configuration, rename it to `style`. The legacy `outputStyle` key is automatically mapped for backward compatibility but should be updated.
* `indentType` and `indentWidth` are no longer supported by the modern sass API. Remove these options if present.
* New option `silenceDeprecations` accepts a list of deprecation IDs (e.g. `['import']`) to suppress specific deprecation warnings.

### SCSS/Sass language changes

The underlying dart-sass compiler has been updated from 1.44 to 1.99. This includes several
language-level deprecations and changes that may affect your stylesheets:

* **`@import` is deprecated.** Migrate to `@use` and `@forward`. To suppress the warnings during migration, add `silenceDeprecations: ['import']` to your sass config. `@import` will be removed in dart-sass 3.0.
* **`/` as division is removed.** Use `math.div($a, $b)` instead of `$a / $b`. You will need `@use 'sass:math'` at the top of the file.
* **Global color functions are deprecated.** Functions like `lighten()`, `darken()`, `adjust-hue()`, `saturate()`, `desaturate()`, `opacify()`, `fade-in()`, `transparentize()`, and `fade-out()` should be replaced with `color.adjust()`, `color.scale()`, or `color.change()` from the `sass:color` module.
* **Strict unary operators.** Ambiguous expressions like `$a -$b` now produce errors. Use `$a - $b` or `$a (-$b)`.

For the full list of dart-sass breaking changes, see:
https://sass-lang.com/documentation/breaking-changes/

Things to be done
-----------------
* Find a way to generate proper source maps with full paths

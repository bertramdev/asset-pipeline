SASS Asset Pipeline
==========================
The `sass-asset-pipeline` is a plugin that provides SASS / SCSS support for the asset-pipeline static asset management plugin using [jsass](https://github.com/bit3/jsass), a Java wrapper around the native library  [libsass](https://github.com/sass/libsass) - the future of Sass compilation.

The version numbering is synced on [jsass](https://github.com/bit3/jsass) versions, the underlying library used by this plugin.

For more information on how to use asset-pipeline, visit [here](http://www.github.com/bertramdev/asset-pipeline).

Configuration
-------------

Configuration is only supported for two properties:
* sass.sourceComments = true
* sass.outputStyle = OutputStyle.EXPANDED, OutputStyle.COMPACT, OutputStyle.COMPRESSED or OutputStyle.NESTED

Usage
-----
Simply create `scss` or `sass` files in your assets folder.

Things to be done
-----------------

* Add more configuration options
* Find a way to generate proper source maps with full paths

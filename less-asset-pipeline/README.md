LESS Asset Pipeline
==========================
The `less-asset-pipeline` is a plugin that provides LESS support for the asset-pipeline static asset management plugin.

For more information on how to use asset-pipeline, visit [here](http://www.github.com/bertramdev/asset-pipeline).

Installation
------------

Add this plugin to your classpath in gradle or dependencies list depending on how you are using it:

```gradle
//Example build.gradle file
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.bertramlabs.plugins:asset-pipeline-gradle:2.8.0'
        classpath 'com.bertramlabs.plugins:less-asset-pipeline:2.8.0'
    }
}
```

Usage
-----

Create files in your standard `assets/stylesheets` folder with extension `.less` or `.css.less`. You also may require other files by using the following requires syntax at the top of each file or the standard LESS import:

```css
/*
*= require test
*= require_self
*= require_tree .
*/

/*Or use this*/
@import 'test'

```


Less4j Support
--------------

This plugin now defaults to compiling your less files with less4j instead of the standard less compiler. To Turn this off you must adjust your config:

```gradle
assets {
    configOptions = [
      less: [
        compiler: 'standard'
      ]
    ]
}
```


Production
----------
During war build your less files are compiled into css files. This is all well and good but sometimes you dont want each individual less file compiled, but rather your main base less file. It may be best to add a sub folder for those LESS files and exclude it in your precompile config...

Sample Gradle Config:
```gradle
  assets {
      excludes = ['mixins/*.less']
  }
```

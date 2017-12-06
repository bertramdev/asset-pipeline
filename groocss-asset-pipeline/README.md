GrooCSS Asset Pipeline
==========================
The `groocss-asset-pipeline` is a plugin that provides GrooCSS support for the asset-pipeline static asset management plugin.

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
        classpath 'com.bertramlabs.plugins:groocss-asset-pipeline:2.8.0'
    }
}
```

Usage
-----

Create files in your standard `assets/stylesheets` folder with extension `.groocss` or `.css.groovy`.
You also may require other files by using the following requires syntax at the top of each file
or the standard GrooCSS import:

```css
importFile 'test.groovy'
```


Configuration Support
--------------

This plugin now defaults to compiling your groocss files without compression, etc. 
To turn this on you must adjust your config:

```gradle
assets {
    configOptions = [
      groocss: [
        compress: true, noExts: true, convertUnderline: true
      ]
    ]
}
```


Production
----------
During war build your groocss files are compiled into css files. This is all well and good but sometimes you dont want each individual groocss file compiled, but rather your main base groocss file. It may be best to add a sub folder for those GrooCSS files and exclude it in your precompile config...

Sample Gradle Config:
```gradle
  assets {
      excludes = ['mixins/*.groocss']
  }
```

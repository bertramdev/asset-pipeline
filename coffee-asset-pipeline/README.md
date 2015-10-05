Coffee Asset-Pipeline
===========================
[![Build Status](https://travis-ci.org/bertramdev/coffee-asset-pipeline.svg?branch=master)](https://travis-ci.org/bertramdev/coffee-asset-pipeline)

Overview
--------
The Coffee Asset-Pipeline module provides coffeescript compilation support for the jvm based asset-pipeline. Simply add this file to your buildscript classpath or development environment and they are automatically processed.

For more information on how to use asset-pipeline, visit [here](http://www.github.com/bertramdev/asset-pipeline).

Installation
------------

Simply add this plugin to your classpath in gradle or dependencies list depending on how you are using it

```gradle
//Example build.gradle file
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.bertramlabs.plugins:asset-pipeline-gradle:2.1.1'
    classpath 'com.bertramlabs.plugins:coffee-asset-pipeline:2.0.6'
  }
}
```

Usage
-----

Simply create files in your standard `assets/javascripts` folder with extension `.coffee` or `.js.coffee`. You also may require other files by using the following requires syntax at the top of each file:

```coffee
#= require test
#= require_self
#= require_tree .
```

*NOTE:* If the command line node coffee command is detected on your system. The application will attempt to use node to compile your javascript instead.

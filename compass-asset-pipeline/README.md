Compass Asset Pipeline
==========================
The `compass-asset-pipeline:2.3.0` is a plugin that provides SASS/Compass support for the asset-pipeline static asset management plugin via compass and jruby.

For more information on how to use asset-pipeline, visit [here](http://www.github.com/bertramdev/asset-pipeline).

Configuration
-------------

This plugin can be configured to load/require alternate gems for use with the sass command.
This can be done via the asset-pipeline config for the respective framework in use:

```groovy
grails.assets.sass.gems = ['bourbon':'4.1.1'] 
```

**NOTE:** This plugin now utilizes `compass:1.0.1` . The previous series of sass-asset-pipeline used `0.7.x`.

Usage
-----
Simply create `scss` or `sass` files in your assets folder. 


Thinks to be done
-----------------

* Fix Compass Sprite Generation
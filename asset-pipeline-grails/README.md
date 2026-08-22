Grails Asset Pipeline
=====================
This repository has moved into the main asset-pipeline repository. Please refer to that repository from now on.

[asset-pipeline](https://github.com/wondrify/asset-pipeline)


Overview
--------
The Grails Asset-Pipeline is a plugin used for managing and processing static assets in [Grails](http://grails.org) applications. Asset-Pipeline functions include processing and minification of both CSS and JavaScript files. It is also capable of being extended to compile custom static assets, such as CoffeeScript or LESS.

For contributions to the core plugin please see the repository for the core plugin at [asset-pipeline](https://github.com/wondrify/asset-pipeline)

Outside Grails
--------------
The tag libraries this plugin carries also work in a Spring Boot application that renders GSP
without being a Grails application. Adding this plugin and `asset-pipeline-spring-boot` to such an
application is enough: the tag libraries become beans of its context, the filter serves what the
pipeline compiled, and `<asset:javascript>` and `<asset:stylesheet>` behave as they do in a Grails
application. Both are needed - this module contributes the tag libraries, the other one configures
the pipeline they read.

A Grails application is unaffected: it finds these tag libraries as artefacts of the plugin, the
way it always has.

Code Documentation
-------------
http://wondrify.github.io/asset-pipeline

User Guide
-------------
https://wondrify.github.io/grails-asset-pipeline/

Things to be Done
-----------------
* Improve SourceMaps


Contributions
-------------
All contributions are of course welcome as this is an ACTIVE project. Any help with regards to reviewing platform compatibility, adding more tests, and general cleanup is most welcome.
Thanks to several people for suggestions throughout development. Notably: Brian Wheeler (@bdwheeler), Rick Jensen (@cdeszaq), Bobby Warner (@bobbywarner), Ted Naleid (@tednaleid) and more to follow I'm sure...

Additional Resources
--------------------
* [Coffeescript Asset-Pipeline Plugin](http://github.com/wondrify/coffee-grails-asset-pipeline)
* [LESS Css Asset-Pipeline Plugin](http://github.com/wondrify/less-grails-asset-pipeline)
* [SASS/SCSS Compass Asset-Pipeline Plugin](http://github.com/wondrify/sass-grails-asset-pipeline)
* [Handlebars Asset-Pipeline Plugin](http://github.com/wondrify/handlebars-grails-asset-pipeline)
* [Ember Asset-Pipeline Plugin](http://github.com/wondrify/ember-grails-asset-pipeline)
* [Rails Asset Pipeline Guide](http://guides.rubyonrails.org/asset_pipeline.html)

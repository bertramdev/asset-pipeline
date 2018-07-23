Ember Asset-Pipeline
=========================
[![Build Status](https://travis-ci.org/bertramdev/ember-asset-pipeline.svg)](https://travis-ci.org/bertramdev/ember-asset-pipeline)

Overview
--------
The JVM `ember-asset-pipeline` is a plugin that provides handlebars template precompiler support  for Ember.js to asset-pipeline.

Current Ember Version: 1.7.0

For more information on how to use asset-pipeline, visit [here](http://www.github.com/bertramdev/asset-pipeline).


Usage
-----

Simply create files in your standard `assets/javascripts` folder with extension `.handlebars` or `.hbs`.
By default the templateRoot for your template names is specified as 'templates'. This means that any handlebars file within the root assets/javascripts folder will utilize its file name (without the extension) as its template name. Or a file in `templates/show.handlebars` would be named `templates/show`. If templates is set as the templateRoot than it would be named `show`

It is also possible to change the template path seperator for templatenames to be used by handlebars:


Gradle Example:

```groovy
assets {
	configOptions = [
	handlebars: [
		templateRoot: 'templates',
		templatePathSeperator: '/'
	]
	]
}
```

Grails Example:
```groovy
grails {
	assets {
		handlebars {
			templateRoot = 'templates'
			templatePathSeperator = "/"
		}
	}
}
```

To use the handlebars runtime simply add handlebars js to your application.js or your gsp file

```javascript
//=require handlebars-runtime
```


Using in the Browser
--------------------

Template functions are stored in the `Handlebars.templates` object using the template name. If the template name is
`person/show`, then the template function can be accessed from `Handlebars.templates['person/show']`. See the Template Names section for how template names are calculated.

See the [Handlebars.js website](http://handlebarsjs.com/) for more information on using Handlebars template functions.

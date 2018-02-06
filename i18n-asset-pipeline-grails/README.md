# I18n asset-pipeline plugin

The Grails plugin `i18n-asset-pipeline` is an asset-pipeline plugin that
generates a JavaScript file with localized texts which can be used for
client-side i18n.

For more information on how to use asset-pipeline, visit
[asset-pipeline project page][asset-pipeline].

## Version information

Because `asset-pipeline` 2.x and 3.x introduced new APIs and aren't backward
compatible, you must use the following versions of this plugin:

i18n-asset-pipeline version | required for
----------------------------|--------------
 0.x                        | `asset-pipeline` up to version 1.9.9
 1.x                        | `asset-pipeline` version 2.0.0 or higher
 2.x                        | Grails 3.x


## Breaking update information 2.1.0
* Removed @import option, replaced with default i18n messages inheritance 
  (i.e. all keys from messages.i18n are merged with the localized version messages_nl.i18n)
* Added support for loading messages.properties from plugins, 
  *Note:* Changes to the messages properties files are not hot reloaded. You need 
  to update the i18n file for this to happen. 
* Added support for custom named i18n properties files (i.e. <asset:i18n name='shared'/>) 
  will actually search for a messages bundle shared.properties
* Added support for regular expressions for selecting key values from the message bundle. 
  simply add `regexp: <regular expression>` in the .i18n file. 
  For example: `regexp: portal\..*` adds all messages starting with `portal.`.  
* Added support for correctly loading the encoding of the messages bundle. 

## Installation

To use this plugin you have to add the following code to your `build.gradle`:

```groovy
buildscript {
    dependencies {
        classpath 'org.amcworld.plugins:i18n-asset-pipeline:2.1.0-SNAPSHOT'
    }
}

dependencies {
    runtime 'org.grails.plugins:i18n-asset-pipeline:2.1.0-SNAPSHOT'
}
```

The first dependency declaration is needed to precompile your assets (e. g.
when building a WAR file).  The second one provides the necessary
`<asset:i18n>` tag and compiles the assets on the fly (e. g. in development)
mode.

## Usage

`i18n-asset-pipeline` uses special files in your asset folders (we recommend
`grails-app/assets/i18n`) with extension '.i18n'.  The names of
these files must contain a language specification separated by underscore, e.
g. `messages_de.i18n` or `messages_en_UK.i18n`.  Files without a language
specification (e. g. `messages.i18n`) are files for the default locale.  These
files mainly contain message codes that are resolved to localized texts.

The plugin generates a JavaScript file, that contains a function named `$L`
which can be called to obtain the localized message by a given code, e. g.:

```javascript
$(".btn").text($L("default.btn.ok"));
```

## I18n file syntax

Each i18n file must be defined according to the following rules:

* Files are line based.
* Lines are trimmed (i. e. leading and terminating whitespaces are removed).
* Empty lines and lines starting with a hash `#` (comment lines) are ignored.
* Lines starting with `regexp:` are matched to all keys from the message file.
  For example: `regexp:.*` adds all keys to the message file. 
* All other lines are treated as messsage codes which are translated to the
  required language.
* Comments after import statements and message codes are not allowed.

Each i18n file may contain asset-pipeline `require` statements to load other
assets such as JavaScript files.  

## Typical file structure

Typically, you have one i18n file for each language in the application.  Given,
you have the following message resources in `grails-app/i18n`:

* `messages.properties`
* `messages_de.properties`
* `messages_en_UK.properties`
* `messages_es.properties`
* `messages_fr.properties`

Then, you should have the same set of files in e. g. `grails-app/assets/i18n`:

* `messages.i18n`
* `messages_de.i18n`
* `messages_en_UK.i18n`
* `messages_es.i18n`
* `messages_fr.i18n`

The codes are can be added manually to each file, but the default inheritance of i18n properties
files are enforced. So the entries of `messages_en_UK.i18n` contains all entries from:

* `messages.i18n`
* `messages_en.i18n`
* `messages_en_UK.i18n`

To add all properties defined in the `messages.properties` bundle simply add `regexp:.*` 
to `messages.i18n` and add empty files for each supported client side locale. 
 
* `messages.i18n` (contains `regexp:.*`)
* `messages_en.i18n` (empty)
* `messages_en_UK.i18n` (empty)
* `messages_nl.i18n` (empty) 

This results in: 

* `messages.js` (all entries from messages.properties)
* `messages_en.js` (all entries from messages.properties merged with messages_en.properties)
* `messages_en_UK.js` (all entries from messages.properties merged with messages_en.properties merged with messages_en_UK.properties)
* `messages_nl.js` (all entries from messages.properties merged with messages_nl.properties) 

## Including localized assets

In order to include a localized asset you can either use an asset-pipeline
`require` directive or the tag `<asset:i18n>`.  The tag supports the following
attributes:

* `locale`.  Either a string or a `java.util.Locale` object representing the
  locale that should be loaded.  This attribute is mandatory.
* `name`.  A string indicating the base name of the i18n files to load
  (defaults to `messages`).

Examples:

```html
<asset:i18n locale="en_UK" />
```

```html
<asset:i18n name="texts" locale="${locale}" />
```

## i18n.js

A more advanced i18n.js is included to add more advanced formatting on the client. 
It can be included with `<asset:javascript src="i18n-asset/i18n.js/>`. 
You can now use `$i18n.m(<code>,<args>,<defaultMessage>)` to retrieve the messages.
Internally $i18n uses $L to get the message. 

* code: the code of the message in the messages resource bundle, required
* args: an array of values to format the message with, optional 
* defaultMessage: a defaultMessage to display if code is not found, defaults to `[<code>]`

For convencience `$i18n.md(<code>,<defaultMessage>)` is added as well.

## Author

This plugin was written by [Daniel Ellermann](mailto:d.ellermann@amc-world.de)
([AMC World Technologies GmbH][amc-world]).

Updated by [Dennie de Lange](mailto:dennie@tkvw.nl).

## License

This plugin was published under the
[Apache License, Version 2.0][apache-license].

[amc-world]: http://www.amc-world.de
[apache-license]: http://www.apache.org/licenses/LICENSE-2.0
[asset-pipeline]: http://www.github.com/bertramdev/asset-pipeline
